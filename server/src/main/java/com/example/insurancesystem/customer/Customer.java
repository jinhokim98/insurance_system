package com.example.insurancesystem.customer;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class Customer {
    private Long id;
    private String name;
    private LocalDate birthDate;
    private Gender gender;
    private LocalDate licenseIssuedDate;
}
