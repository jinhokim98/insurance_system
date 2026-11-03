package com.example.insurancesystem.product;

public record CreateInsuranceProductRequest (
        String name,
        String description,
        long coverageLimit,
        long premium,
        int coveragePeriodDays
) {

}
