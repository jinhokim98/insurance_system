package com.example.insurancesystem.contract;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.net.URI;

@RestController
@RequestMapping("/api")
public class InsuranceContractController {
    private final InsuranceContractRepository repository;
    private final InsuranceContractService insuranceContractService;

    public InsuranceContractController(InsuranceContractRepository repository, InsuranceContractService insuranceContractService) {
        this.repository = repository;
        this.insuranceContractService = insuranceContractService;
    }

    @GetMapping("/customer/{customerId}/contracts")
    public ResponseEntity<List<InsuranceContract>> getCustomerContracts(
            @PathVariable("customerId") long customerId
    ) {
        if (customerId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(repository.findByCustomerId(customerId));
    }

    @GetMapping("/contracts/{id}")
    public ResponseEntity<InsuranceContract> getContract(@PathVariable("id") long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/contract")
    public ResponseEntity<Long> createContract(@RequestBody CreateContractRequest request) {
        long id = insuranceContractService.createContract(request);

        return ResponseEntity.created(URI.create("/api/contracts/" + id)).body(id);
    }
}
