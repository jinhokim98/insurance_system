package com.example.insurancesystem.receipt;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class Receipt {
	private Long id;
	private Long contractId;
	private LocalDateTime crashTime;
	private String crashPlace;
	private String crashDetail;
	private Long billAmount;
}
