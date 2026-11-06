package com.example.insurancesystem.contract;

import com.example.insurancesystem.customer.Customer;
import com.example.insurancesystem.customer.CustomerRepository;
import com.example.insurancesystem.product.InsuranceProduct;
import com.example.insurancesystem.product.InsuranceProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.web.server.ResponseStatusException;

@Service
public class InsuranceContractService {
    private final CustomerRepository customerRepository;
    private final InsuranceProductRepository productRepository;
    private final InsuranceContractRepository insuranceContractRepository;

    public InsuranceContractService(
            CustomerRepository customerRepository,
            InsuranceProductRepository insuranceProductRepository,
            InsuranceContractRepository insuranceContractRepository
    ) {
        this.customerRepository = customerRepository;
        this.productRepository = insuranceProductRepository;
        this.insuranceContractRepository = insuranceContractRepository;
    }

    @Transactional
    public long createContract(CreateContractRequest request) {
        if (request.customerId() <= 0 || request.productId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "고객과 상품 ID가 유효하지 않습니다.");
        }
        if (request.vehicleNumber() == null || request.vehicleNumber().isBlank()
                || request.vehicleNumber().codePointCount(0, request.vehicleNumber().length()) > 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "차량번호는 1~20자로 입력해 주세요.");
        }
        if (request.coverageStartDate() == null
                || !request.coverageStartDate().isAfter(LocalDate.now(ZoneId.of("Asia/Seoul")))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "보장 시작일은 내일 이후여야 합니다.");
        }

        Customer customer = customerRepository
                .findById(request.customerId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "고객을 찾을 수 없습니다."
                ));

        InsuranceProduct product = productRepository
                .findById(request.productId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "상품을 찾을 수 없습니다."
                ));

        // 중복검사
        LocalDateTime startsAt = request.coverageStartDate().atStartOfDay();
        LocalDateTime endsAt = startsAt.plusDays(product.getCoveragePeriodDays());
        boolean overlapping = insuranceContractRepository.existsOverlapping(
                request.vehicleNumber().strip(),
                startsAt,
                endsAt
        );

        if (overlapping) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "해당 차량에 보장 기간이 겹치는 계약이 있습니다."
            );
        }

        NewInsuranceContract contract = new NewInsuranceContract(
                customer.getId(), product.getId(), request.vehicleNumber().strip(),
                startsAt, endsAt, product.getCoverageLimit(), product.getPremium(),
                ContractStatus.PENDING_PAYMENT
        );
        return insuranceContractRepository.create(contract);
    }
}
