package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransactionInputDetails;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.repository.AccountsRepository;
import com.db.awmd.challenge.service.AccountsService;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class AccountsControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private AccountsService accountsService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private TransactionInputDetails transactionInputDetails;

    @Before
    public void prepareMockMvc() {
        this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

        // Reset the existing accounts before each test.
        accountsRepository.clearAccounts();
    }

    @Test
    public void createAccount() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

        Account account = accountsService.getAccount("Id-123");
        assertThat(account.getAccountId()).isEqualTo("Id-123");
        assertThat(account.getBalance()).isEqualByComparingTo("1000");
    }

    @Test
    public void createDuplicateAccount() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
    }

    @Test
    public void createAccountNoAccountId() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"balance\":1000}")).andExpect(status().isBadRequest());
    }

    @Test
    public void createAccountNoBalance() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
    }

    @Test
    public void createAccountNoBody() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createAccountNegativeBalance() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
    }

    @Test
    public void createAccountEmptyAccountId() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
    }

    @Test
    public void getAccount() throws Exception {
        String uniqueAccountId = "Id-" + System.currentTimeMillis();
        Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
        this.accountsService.createAccount(account);
        this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
                .andExpect(status().isOk())
                .andExpect(
                        content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
    }

    @Test
    public void transferAmount() throws Exception {
        String accountFromId = "Id-121";
        String accountToId = "Id-212";

        Account account0 = new Account(accountFromId, new BigDecimal("10000.45"));
        Account account1 = new Account(accountToId, new BigDecimal("5000.35"));
        this.accountsService.createAccount(account0);
        this.accountsService.createAccount(account1);
        this.mockMvc.perform(post("/v1/accounts/transferAmount").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountFromId\":\"Id-121\",\"accountToId\":\"Id-212\",\"amountToTransfer\":1000}")).andExpect(status().isAccepted());
    }

    @Test
    public void transferAmountDuplicateId() throws Exception {
        String accountFromId = "Id-141";
        String accountToId = "Id-141";

        Account account0 = new Account(accountFromId, new BigDecimal("10000.45"));
        Account account1 = new Account(accountToId, new BigDecimal("5000.35"));
        try {
            this.accountsService.createAccount(account0);
            this.accountsService.createAccount(account1);
            this.mockMvc.perform(post("/v1/accounts/transferAmount").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"accountFromId\":\"Id-141\",\"accountToId\":\"Id-141\",\"amountToTransfer\":1000}")).andExpect(status().isBadRequest());
            fail("Should have failed when adding duplicate account");
        } catch (DuplicateAccountIdException ex) {
            assertThat(ex.getMessage()).isEqualTo("Account id " + accountFromId + " already exists!");
        }
    }

    @Test
    public void transferAmountInsufficientFunds() throws Exception {
        String accountFromId = "Id-181";
        String accountToId = "Id-182";

        Account account0 = new Account(accountFromId, new BigDecimal("1000.45"));
        Account account1 = new Account(accountToId, new BigDecimal("7000.35"));
        this.accountsService.createAccount(account0);
        this.accountsService.createAccount(account1);
        this.mockMvc.perform(post("/v1/accounts/transferAmount").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountFromId\":\"Id-181\",\"accountToId\":\"Id-182\",\"amountToTransfer\":4000}")).andExpect(status().isBadRequest());
    }

}
