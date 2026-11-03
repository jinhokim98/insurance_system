package com.example.insurancesystem.product;

import org.springframework.http.ResponseEntity;
import org.springframework.http.ProblemDetail;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
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
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Void> createProduct(
            @RequestBody CreateInsuranceProductRequest request
    ) {
        if (isInvalidName(request.name()) ||
                request.coverageLimit() <= 0 ||
                request.premium() <= 0 || request.coveragePeriodDays() <= 0) {
            return ResponseEntity.badRequest().build();
        }
        repository.save(request);
        return ResponseEntity.status(201).build();
    }

    @PatchMapping
    public ResponseEntity<Void> patchProduct(
            @RequestBody PatchInsuranceProductRequest request
    ) {
        if (request.id() <= 0 || isInvalidName(request.name())) {
            return ResponseEntity.badRequest().build();
        }

        int count = repository.patch(request);
        if (count == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(200).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable("id") long id
    ) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        int count;
        try {
            count = repository.delete(id);
        } catch (DataIntegrityViolationException exception) {
            throw new ProductDeletionConflictException(exception);
        }
        if (count == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(204).build();
    }

    private boolean isInvalidName(String name) {
        return name == null || name.isBlank()
                || name.codePointCount(0, name.length()) > 100;
    }

    @ExceptionHandler(ProductDeletionConflictException.class)
    public ResponseEntity<ProblemDetail> handleDeletionConflict(ProductDeletionConflictException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, "다른 데이터에서 참조하는 상품은 삭제할 수 없습니다."
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    private static class ProductDeletionConflictException extends RuntimeException {
        ProductDeletionConflictException(Throwable cause) {
            super(cause);
        }
    }
}
