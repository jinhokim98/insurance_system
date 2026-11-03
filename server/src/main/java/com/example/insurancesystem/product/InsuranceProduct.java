package com.example.insurancesystem.product;

public class InsuranceProduct {
    private Long id;
    private String name;
    private String description;
    private long coverageLimit; // 보장 한도
    private long premium; // 고정 보험료
    private int coveragePeriodDays; // 보장 기간(일)

    public InsuranceProduct(
            Long id,
            String name,
            String description,
            long coverageLimit,
            long premium,
            int coveragePeriodDays
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.coverageLimit = coverageLimit;
        this.premium = premium;
        this.coveragePeriodDays = coveragePeriodDays;
    }

    public Long  getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public Long getCoverageLimit() {
        return coverageLimit;
    }
    public long getPremium() {
        return premium;
    }
    public int getCoveragePeriodDays() {
        return coveragePeriodDays;
    }
}
