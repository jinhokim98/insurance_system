package com.example.insurancesystem.receipt;

import com.example.insurancesystem.manager.Manager;
import com.example.insurancesystem.manager.ManagerRepository;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/receipt")
public class ReceiptController {
	private final ReceiptService receiptService;
	private final ManagerRepository managerRepository;

	public ReceiptController(ReceiptService receiptService, ManagerRepository managerRepository) {
		this.receiptService = receiptService;
		this.managerRepository = managerRepository;
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

	@GetMapping("/status")
	public ResponseEntity<List<Receipt>> getReceiptsByReceiptStatus(
			@RequestParam("receiptStatus") ReceiptStatus receiptStatus
	) {
		return ResponseEntity.ok(receiptService.findByReceiptStatus(receiptStatus));
	}

	@PostMapping("/{receiptId}/assign-receipt")
	public ResponseEntity<Long> assignManager(@PathVariable Long receiptId) {
		Optional<Manager> manager = managerRepository.findSpareMember();
		long managerId = manager.map(Manager::getId).orElse(1L);
		receiptService.assignManager(receiptId, managerId);
		return ResponseEntity.ok().body(managerId);
	}

}
