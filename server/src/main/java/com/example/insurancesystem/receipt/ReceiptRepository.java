package com.example.insurancesystem.receipt;

import java.util.Optional;
import java.util.List;

public interface ReceiptRepository {
	List<Receipt> findByCustomerId(long customerId);

	List<Receipt> findByContractId(long contractId);

	Optional<Receipt> findById(long id);

	Receipt createReceipt(ReceiptRequest request);

	int updateReceipt(long id, ReceiptRequest request);

	int deleteReceipt(long id);

	List<Receipt> findByReceiptStatus(ReceiptStatus receiptStatus);
}
