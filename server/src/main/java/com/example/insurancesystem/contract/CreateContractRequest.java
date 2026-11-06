package com.example.insurancesystem.contract;

import java.time.LocalDate;

public record CreateContractRequest(
        long customerId,
        long productId,
        String vehicleNumber,
        LocalDate coverageStartDate
) {
}
