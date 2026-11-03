package com.example.insurancesystem.contract;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class InsuranceContract {
    private long id;
    private long customerId;
    private long productId;
    private LocalDateTime createdAt;
    private LocalDateTime coverageStartsAt;
    private LocalDateTime coverageEndsAt;
    private String vehicleNumber;
    private long coverageLimit; // 보장 한도
    private long premium; // 고정 보험료
    private ContractStatus status;
}
