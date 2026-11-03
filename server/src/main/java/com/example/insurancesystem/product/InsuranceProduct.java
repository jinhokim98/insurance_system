package com.example.insurancesystem.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InsuranceProduct {
    private Long id;
    private String name;
    private String description;
    private long coverageLimit; // 보장 한도
    private long premium; // 고정 보험료
    private int coveragePeriodDays; // 보장 기간(일)
}
