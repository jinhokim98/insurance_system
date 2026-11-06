package com.example.insurancesystem.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PremiumPayment {
    private long id;
    private long contractId;
    private long amount;
    private String paymentMethod;
    private LocalDateTime paidAt;
}
