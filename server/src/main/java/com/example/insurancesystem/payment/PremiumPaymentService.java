package com.example.insurancesystem.payment;

import com.example.insurancesystem.contract.ContractStatus;
import com.example.insurancesystem.contract.InsuranceContract;
import com.example.insurancesystem.contract.InsuranceContractRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class PremiumPaymentService {
    private final PremiumPaymentRepository premiumPaymentRepository;
    private final InsuranceContractRepository insuranceContractRepository;

    public PremiumPaymentService(PremiumPaymentRepository premiumPaymentRepository, InsuranceContractRepository insuranceContractRepository) {
        this.premiumPaymentRepository = premiumPaymentRepository;
        this.insuranceContractRepository = insuranceContractRepository;
    }

    @Transactional
    public PremiumPayment pay(long contractId) {
        if (contractId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "계약 ID가 유효하지 않습니다."
            );
        }

        InsuranceContract contract = insuranceContractRepository
                .findById(contractId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "계약을 찾을 수 없습니다."
                ));

        // 이미 냈으면 기존 기록 반환
        Optional<PremiumPayment> existingPayment = premiumPaymentRepository.findByContractId(contractId);
        if (existingPayment.isPresent()) {
            return existingPayment.get();
        }

        // 납부 가능한 상태 확인
        if (contract.getStatus() != ContractStatus.PENDING_PAYMENT) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "납부 대기 중인 계약만 납부할 수 있습니다."
            );
        }

        premiumPaymentRepository.save(contractId, contract.getPremium(), "MOCK");

        int updated = insuranceContractRepository.activate(contractId);

        if (updated != 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "계약 상태가 변경되어 납부를 완료할 수 없습니다."
            );
        }

        return premiumPaymentRepository.findByContractId(contractId)
                .orElseThrow(() -> new IllegalStateException(
                        "저장한 납부 기록을 찾을 수 없습니다."
                ));

    }
}
