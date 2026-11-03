package com.example.insurancesystem.contract;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class InsuranceContractController {
    private final InsuranceContractRepository repository;

    public InsuranceContractController(InsuranceContractRepository repository) {
        this.repository = repository;
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
}
