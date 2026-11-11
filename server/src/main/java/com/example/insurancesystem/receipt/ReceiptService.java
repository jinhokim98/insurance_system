package com.example.insurancesystem.receipt;

import java.lang.reflect.Member;
import java.util.List;
import java.util.Optional;

public interface ReceiptService {
	List<Receipt> findByReceiptStatus(ReceiptStatus receiptStatus);

	Receipt createReceipt(ReceiptRequest request);

	Optional<Receipt> findById(long receiptId);

	List<Receipt> findByCustomerId(long customerId);

	void assignManager(long receiptId, long managerId);

	void acceptReceipt(long receiptId, long managerId);

	void deniedReceipt(long receiptId, long managerId);
}
