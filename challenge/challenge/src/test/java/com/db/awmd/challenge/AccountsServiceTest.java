package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransactionInputDetails;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InsufficientFundsException;
import com.db.awmd.challenge.service.AccountsService;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

    @Autowired
    private AccountsService accountsService;

    @Autowired
    private TransactionInputDetails transactionInputDetails;


    @Test
    public void addAccount() throws Exception {
        Account account = new Account("Id-123");
        account.setBalance(new BigDecimal(1000));
        this.accountsService.createAccount(account);

        assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
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

    @Test
    public void transferAmount() throws Exception {
        String accountFromId = "Id-101";
        String accountToId = "Id-102";

        Account account0 = new Account(accountFromId, new BigDecimal("10000.45"));
        Account account1 = new Account(accountToId, new BigDecimal("5000.35"));
        this.accountsService.createAccount(account0);
        this.accountsService.createAccount(account1);

        transactionInputDetails.setAccountFromId(accountFromId);
        transactionInputDetails.setAccountToId(accountToId);
        transactionInputDetails.setAmountToTransfer(new BigDecimal("2000.35"));
        this.accountsService.transferAmount(transactionInputDetails);

        assertThat(HttpStatus.ACCEPTED);
    }

    @Test
    public void transferAmountDuplicateId() throws Exception {
        String accountFromId = "Id-111";
        String accountToId = "Id-111";

        Account account0 = new Account(accountFromId, new BigDecimal("10000.45"));
        Account account1 = new Account(accountToId, new BigDecimal("5000.35"));

        try {
            this.accountsService.createAccount(account0);
            this.accountsService.createAccount(account1);
            this.accountsService.transferAmount(transactionInputDetails);
            fail("Should have failed when adding duplicate account");
        } catch (DuplicateAccountIdException ex) {
            assertThat(ex.getMessage()).isEqualTo("Account id " + accountFromId + " already exists!");
        }
    }

    @Test
    public void transferAmountInsufficientFunds() throws Exception {
        String accountFromId = "Id-121";
        String accountToId = "Id-122";

        Account account0 = new Account(accountFromId, new BigDecimal("1000.45"));
        Account account1 = new Account(accountToId, new BigDecimal("5000.35"));
        this.accountsService.createAccount(account0);
        this.accountsService.createAccount(account1);


        transactionInputDetails.setAccountFromId(accountFromId);
        transactionInputDetails.setAccountToId(accountToId);
        transactionInputDetails.setAmountToTransfer(new BigDecimal("7000.35"));

        try {
            this.accountsService.transferAmount(transactionInputDetails);
            fail("Should have failed when accountFromId has insufficient funds");
        } catch (InsufficientFundsException ex) {
            assertThat(ex.getMessage()).isEqualTo("Transaction Declined: Transfer Amount should always be less than the balance amount of " + accountFromId);
        }
    }


}
