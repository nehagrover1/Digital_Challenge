package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.service.AccountsService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

	@Autowired
	private AccountsService accountsService;

	@Test
	public void addAccount() throws Exception {
		Account account = new Account("Id-123");
		account.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(account);

		assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
	}

	@Test
	public void initiateTransfer() throws Exception {
		Account firstAccount = new Account("Id-123");
		firstAccount.setBalance(new BigDecimal(1000));
		Account secondAccount = new Account("ID-235");
		secondAccount.setBalance(new BigDecimal(500));
		this.accountsService.createAccount(firstAccount);
		this.accountsService.createAccount(secondAccount);
		int success = this.accountsService.initiateTransfer(firstAccount.getAccountId(), secondAccount.getAccountId(),
				200);
		assertTrue((success) == 0);
	}

	@Test
	public void initiateTransfer_insufficientBalance() throws Exception {

		try {
			Account firstAccount = new Account("Id-123");
			firstAccount.setBalance(new BigDecimal(1000));
			Account secondAccount = new Account("ID-235");
			this.accountsService.createAccount(firstAccount);
			this.accountsService.createAccount(secondAccount);
			secondAccount.setBalance(new BigDecimal(500));
			this.accountsService.initiateTransfer(firstAccount.getAccountId(), secondAccount.getAccountId(), 5000);

		} catch (IllegalArgumentException ex) {
			assertThat(ex.getMessage()).isEqualTo("Transfer cannot be completed.InsufficientBalance");
		}

	}

	@Test
	public void addAccount_failsOnDuplicateId() throws Exception {
		String uniqueId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueId);
		this.accountsService.createAccount(account);

		try {
			this.accountsService.createAccount(account);
			fail("Should have failed when adding duplicate account");
		} catch (DuplicateAccountIdException ex) {
			assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
		}

	}
}
