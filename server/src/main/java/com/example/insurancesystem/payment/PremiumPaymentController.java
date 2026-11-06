package com.example.insurancesystem.payment;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PremiumPaymentController {
    private final PremiumPaymentService premiumPaymentService;

    public PremiumPaymentController(PremiumPaymentService premiumPaymentService) {
        this.premiumPaymentService = premiumPaymentService;
    }

    @PostMapping("/contracts/{contractId}/mock-payment")
    public ResponseEntity<PremiumPayment> pay(@PathVariable("contractId") long contractId) {
        PremiumPayment payment = premiumPaymentService.pay(contractId);
        return ResponseEntity.ok(payment);
    }

}
