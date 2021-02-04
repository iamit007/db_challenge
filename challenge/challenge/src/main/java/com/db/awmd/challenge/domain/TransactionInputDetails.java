package com.db.awmd.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Repository
public class TransactionInputDetails {

    //    No args constructor
    public TransactionInputDetails() {
    }

    ;

    //   Parameterized constructor
    public TransactionInputDetails(String accountFromId) {
        this.accountFromId = accountFromId;
        this.accountToId = getAccountToId();
        this.amountToTransfer = getAmountToTransfer();
    }

    //    All Args JsonCreator Constructor
    @JsonCreator
    public TransactionInputDetails(
            @JsonProperty("accountFromId") String accountFromId,
            @JsonProperty("accountToId") String accountToId,
            @JsonProperty("amountToTransfer") BigDecimal amountToTransfer) {
        this.accountFromId = accountFromId;
        this.accountToId = accountToId;
        this.amountToTransfer = amountToTransfer;
    }

    ;
    //  Transaction fields
    @NotNull
    @NotEmpty
    private String accountFromId;

    @NotNull
    @NotEmpty
    private String accountToId;

    @NotNull
    @Min(value = 1, message = "Minimum amount to transfer should not be less than 1")
    private BigDecimal amountToTransfer;

    public String getAccountFromId() {
        return accountFromId;
    }

    public String getAccountToId() {
        return accountToId;
    }

    public BigDecimal getAmountToTransfer() {
        return amountToTransfer;
    }

    public void setAmountToTransfer(BigDecimal amountToTransfer) {
        this.amountToTransfer = amountToTransfer;
    }

    public void setAccountFromId(String accountFromId) {
        this.accountFromId = accountFromId;
    }

    public void setAccountToId(String accountToId) {
        this.accountToId = accountToId;
    }
}
