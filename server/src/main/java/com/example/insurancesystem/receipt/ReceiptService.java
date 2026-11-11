package com.example.insurancesystem.receipt;

import java.util.List;
import java.util.Optional;

public interface ReceiptService {
	Receipt createReceipt(ReceiptRequest request);

	Optional<Receipt> findById(long receiptId);

	List<Receipt> findByCustomerId(long customerId);
}
