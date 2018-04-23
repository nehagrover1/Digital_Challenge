package com.db.awmd.challenge.service;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.db.awmd.challenge.domain.Account;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EmailNotificationService implements NotificationService {

  @Override
  public void notifyAboutTransfer(Account account, String transferDescription) {
    //THIS METHOD SHOULD NOT BE CHANGED - ASSUME YOUR COLLEAGUE WILL IMPLEMENT IT
	  System.out.println("Sending notification to owner of {}: {}"+account.getAccountId()+ transferDescription);
    log
      .info("Sending notification to owner of {}: {}", account.getAccountId(), transferDescription);
 
  }

}
