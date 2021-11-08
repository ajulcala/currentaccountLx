package com.currentaccount.app.models.documents;

import com.currentaccount.app.models.dto.Customer;
import com.currentaccount.app.models.dto.Managers;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@Builder
@Document("current_account")
@AllArgsConstructor
@NoArgsConstructor
public class CurrentAccount {
    @Id
    private String id;
    @NotNull
    private Customer customer;
    @NotNull
    private String cardNumber;
    @NotNull
    private Integer freeTransactions;
    @NotNull
    private Double commissionTransactions;
    @NotNull
    private Double commissionMaintenance;
    private Double balance;
    private LocalDateTime createAt;
    private List<Managers> owners;
    private List<Managers> signatories;
}
