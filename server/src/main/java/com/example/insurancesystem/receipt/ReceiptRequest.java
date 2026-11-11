package com.example.insurancesystem.receipt;

import java.time.LocalDateTime;

public record ReceiptRequest(
		long contractId,
		long customerId,
		LocalDateTime crashTime,
		String crashPlace,
		String crashDetail,
		Long billAmount
) {
}
