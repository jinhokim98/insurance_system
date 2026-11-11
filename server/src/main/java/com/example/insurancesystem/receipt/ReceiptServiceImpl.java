package com.example.insurancesystem.receipt;

import com.example.insurancesystem.contract.ContractStatus;
import com.example.insurancesystem.contract.InsuranceContract;
import com.example.insurancesystem.contract.InsuranceContractRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class ReceiptServiceImpl implements ReceiptService {
	private final ReceiptRepository receiptRepository;
	private final InsuranceContractRepository insuranceContractRepository;

	public ReceiptServiceImpl(ReceiptRepository receiptRepository, InsuranceContractRepository insuranceContractRepository) {
		this.receiptRepository = receiptRepository;
		this.insuranceContractRepository = insuranceContractRepository;
	}

	@Override
	public List<Receipt> findByReceiptStatus(ReceiptStatus receiptStatus) {
		if (receiptStatus == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "접수 상태를 선택해 주세요.");
		}
		return receiptRepository.findByReceiptStatus(receiptStatus);
	}

	@Override
	public Receipt createReceipt(ReceiptRequest request) {
		// 입력값 검증
		if (request.customerId() <= 0 || request.contractId() <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "고객과 계약을 선택해 주세요.");
		}
		if (request.crashTime() == null || request.crashTime().isAfter(LocalDateTime.now(ZoneId.of("Asia/Seoul")))) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사고 시각은 필수이며 미래일 수 없습니다.");
		}
		if (request.crashPlace() == null || request.crashPlace().isBlank()
				|| request.crashDetail() == null || request.crashDetail().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사고 장소와 경위를 입력해 주세요.");
		}
		if (request.billAmount() != null && request.billAmount() < 0) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"예상 금액은 음수일 수 없습니다."
			);
		}

		// 계약 조회
		InsuranceContract contract = insuranceContractRepository.findById(request.contractId()).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "계약을 찾을 수 없습니다.")
		);

		if (contract.getCustomerId() != request.customerId()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "선택한 고객의 계약이 아닙니다.");
		}
		if (contract.getCoverageStartsAt() == null || contract.getCoverageEndsAt() == null
				|| request.crashTime().isBefore(contract.getCoverageStartsAt())
				|| request.crashTime().isAfter(contract.getCoverageEndsAt())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사고 시각이 계약의 보장 기간 안에 있어야 합니다.");
		}

		// 납부 완료 상태 확인
		if (contract.getStatus() != ContractStatus.ACTIVE) {
			throw new ResponseStatusException(
					HttpStatus.CONFLICT,
					"보험료 납부가 완료된 계약만 접수할 수 있습니다."
			);
		}

		return receiptRepository.createReceipt(request);
	}

	@Override
	public Optional<Receipt> findById(long receiptId) {
		if (receiptId <= 0) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"접수 id가 유효하지 않습니다."
			);
		}

		return receiptRepository.findById(receiptId);
	}

	@Override
	public List<Receipt> findByCustomerId(long customerId) {
		if (customerId <= 0) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"고객 ID가 유효하지 않습니다."
			);
		}
		return receiptRepository.findByCustomerId(customerId);
	}


}
