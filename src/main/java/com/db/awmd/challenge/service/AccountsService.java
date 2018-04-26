package com.db.awmd.challenge.service;

import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.repository.AccountsRepository;

import lombok.Getter;

@Service
public class AccountsService {

	@Getter
	private final AccountsRepository accountsRepository;

	@Getter
	private final NotificationService notificationService;

	@Autowired
	public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
		this.accountsRepository = accountsRepository;
		this.notificationService = notificationService;
	}

	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	public Account getAccount(String accountId) {
		return this.accountsRepository.getAccount(accountId);
	}

	private final Random number = new Random(123L);

	/**
	 * Method to Deposit Amount from one Account to another.
	 * 
	 * @param first
	 * @param second
	 * @param amount
	 * @return
	 * @throws InterruptedException
	 */
	private double depositAmount(Account first, Account second, double amount) throws InterruptedException {
		while (true) {
			if (first.getLock().tryLock()) {
				try {
					if (second.getLock().tryLock()) {
						try {
							if (amount > first.getBalance().doubleValue()) {
								throw new IllegalArgumentException("Transfer cannot be completed.InsufficientBalance.");
							}
							second.setBalance(new BigDecimal(second.getBalance().doubleValue() + amount));
							first.setBalance(new BigDecimal(first.getBalance().doubleValue() - amount));
							break;
						} finally {
							second.getLock().unlock();
						}
					}
				} finally {
					first.getLock().unlock();
				}
			}
			int n = number.nextInt(1000);
			int TIME = 1000 + n; // 1 second + random delay to prevent livelock
			Thread.sleep(TIME);
		}

		return first.getBalance().doubleValue();
	}

	/**
	 * Method to Initiate Transfer from one account to another
	 * 
	 * @param firstAccountId
	 * @param secondAccountId
	 * @param amount
	 * @return
	 */
	public int initiateTransfer(String firstAccountId, String secondAccountId, final double amount) {

		int result = 0;
		Callable<Integer> callableObj = () -> {
			try {
				depositAmount(accountsRepository.getAccount(firstAccountId),
						accountsRepository.getAccount(secondAccountId), amount);
				notificationService.notifyAboutTransfer(accountsRepository.getAccount(firstAccountId),
						"Transferred Amount" + amount + "To AccountID " + secondAccountId);
				notificationService.notifyAboutTransfer(accountsRepository.getAccount(secondAccountId),
						"Received Amount" + amount + "From AccountID " + firstAccountId);
				System.out.println("First account" + accountsRepository.getAccount(firstAccountId).getBalance()
						+ "jshaj" + accountsRepository.getAccount(secondAccountId).getBalance());
				return 0;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt(); // Reset interrupted
				return 1; // Error
			}
		};

		ExecutorService service = Executors.newSingleThreadExecutor();
		Future<Integer> future = service.submit(callableObj);
		try {
			result = future.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		service.shutdown();
		return result;

	}

}
