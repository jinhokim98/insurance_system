package com.example.insurancesystem.product;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class InsuranceProductController {
    private final InsuranceProductRepository repository;

    public InsuranceProductController(
            InsuranceProductRepository repository
    ) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<InsuranceProduct>> getProducts() {
        List<InsuranceProduct> products = repository.findAll();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InsuranceProduct> getProduct(
            @PathVariable("id") long id
    ) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<InsuranceProduct> createProduct(
            @RequestBody CreateInsuranceProductRequest request
    ) {
        if (request.name() == null || request.name().isBlank() ||
                request.coverageLimit() <= 0 ||
                request.premium() <= 0 || request.coveragePeriodDays() <= 0) {
            return ResponseEntity.badRequest().build();
        }
        repository.save(request);
        return ResponseEntity.status(201).build();
    }

    @PatchMapping
    public ResponseEntity<InsuranceProduct> patchProduct(
            @RequestBody PatchInsuranceProductRequest request
    ) {
        if (request.name() == null || request.name().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        int count = repository.patch(request);
        if (count == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(200).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<InsuranceProduct> deleteProduct(
            @PathVariable("id") long id
    ) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        int count = repository.delete(id);
        if (count == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(204).build();
    }
}