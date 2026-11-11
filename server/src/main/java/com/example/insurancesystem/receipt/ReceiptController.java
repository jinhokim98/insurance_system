package com.example.insurancesystem.receipt;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/receipt")
public class ReceiptController {
	private final ReceiptService receiptService;

	public ReceiptController(ReceiptService receiptService) {
		this.receiptService = receiptService;
	}

	@GetMapping("/{receiptId}")
	public ResponseEntity<Receipt> getReceipt(@PathVariable("receiptId") long receiptId) {
		return receiptService.findById(receiptId).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@GetMapping
	public ResponseEntity<List<Receipt>> getReceipts(@RequestParam("customerId") long customerId) {
		return ResponseEntity.ok(receiptService.findByCustomerId(customerId));
	}

	@PostMapping
	public ResponseEntity<Receipt> createReceipt(@RequestBody ReceiptRequest request) {
		Receipt receipt = receiptService.createReceipt(request);
		return ResponseEntity.created(URI.create("/api/receipt/" + receipt.getId())).body(receipt);
	}
}
