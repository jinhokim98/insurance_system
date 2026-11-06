package com.example.insurancesystem.contract;

import java.time.LocalDateTime;

public record NewInsuranceContract(
        long customerId,
        long productId,
        String vehicleNumber,
        LocalDateTime coverageStartsAt,
        LocalDateTime coverageEndsAt,
        long coverageLimit,
        long premium,
        ContractStatus status
) {
}
