package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransactionInputDetails;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InsufficientFundsException;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@Service
public class AccountsService {

    @Getter
    private final AccountsRepository accountsRepository;

    @Getter
    private final TransactionInputDetails transactionInputDetails;

    @Autowired
    public AccountsService(AccountsRepository accountsRepository, TransactionInputDetails transactionInputDetails) {
        this.accountsRepository = accountsRepository;
        this.transactionInputDetails = transactionInputDetails;
    }

    @Getter
    public Account account;

    public void createAccount(Account account) {
        this.accountsRepository.createAccount(account);
    }

    public Account getAccount(String accountId) {
        return this.accountsRepository.getAccount(accountId);
    }

    private static long uniqueTransactionId = 0;

    //    It will handle multiple calls but the transfer will be synchronized
    @Async
    public ResponseEntity<Object> transferAmount(@RequestBody TransactionInputDetails transactionInputDetails) {

        String transferDescription = null; //will be used for sending email notification
        String accountFromId = transactionInputDetails.getAccountFromId();
        String accountToId = transactionInputDetails.getAccountToId();
        BigDecimal amountToTransfer = transactionInputDetails.getAmountToTransfer();
        BigDecimal accountFromIdbalance = accountsRepository.getAccount(accountFromId).getBalance();

        if (accountToId.equals(accountFromId)) {
            throw new DuplicateAccountIdException("Accounts" + transactionInputDetails.getAccountToId() + "and" + transactionInputDetails.getAccountFromId() + " cannot be same!");
        } else if (accountsRepository.getAccount(accountFromId).getBalance().compareTo(amountToTransfer) <= 0) {
            throw new InsufficientFundsException("Transaction Declined: Transfer Amount should always be less than the balance amount of " + accountFromId);
        } else {
            synchronized (this) {
                accountFromIdbalance = accountFromIdbalance.subtract(amountToTransfer);
            }
        }
        transferDescription = "Transaction of INR " + amountToTransfer + " successful with Transaction ID: " + getUniqueTransactionId() + "  Remaining balance is " + accountFromIdbalance;
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    //To get Unique Transaction Id
    private static synchronized String getUniqueTransactionId() {
        return String.valueOf(uniqueTransactionId++);
    }
}
