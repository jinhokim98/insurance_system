package com.example.insurancesystem.receipt;

import com.example.insurancesystem.contract.ContractStatus;
import com.example.insurancesystem.contract.InsuranceContract;
import com.example.insurancesystem.contract.InsuranceContractRepository;
import com.example.insurancesystem.manager.Manager;
import com.example.insurancesystem.manager.ManagerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class ReceiptServiceImpl implements ReceiptService {
	private final ReceiptRepository receiptRepository;
	private final InsuranceContractRepository insuranceContractRepository;
	private final ManagerRepository managerRepository;

	public ReceiptServiceImpl(ReceiptRepository receiptRepository, InsuranceContractRepository insuranceContractRepository, ManagerRepository managerRepository) {
		this.receiptRepository = receiptRepository;
		this.insuranceContractRepository = insuranceContractRepository;
		this.managerRepository = managerRepository;
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

	private Manager validateManager(Long managerId) {
		return managerRepository
				.findById(managerId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "매니저가 없습니다."));
	}

	private Receipt validateReceipt(Long receiptId) {
		return receiptRepository
				.findById(receiptId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "접수가 없습니다."));
	}

	@Override
	@Transactional
	public void assignManager(long receiptId, long managerId) {
		if (receiptId <= 0 || managerId <= 0) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"ID가 유효하지 않습니다."
			);
		}

		Manager manager = validateManager(managerId);
		Receipt receipt = validateReceipt(receiptId);
		try {
            receipt.assignManager(manager.getId());
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
        }

        if (receiptRepository.assignReceipt(receipt.getId(), receipt.getManagerId()) != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "다른 요청이 접수 상태 또는 담당자를 변경했습니다.");
        }
	}

	@Override
	public void acceptReceipt(long receiptId, long managerId) {
		throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "심사 승인은 아직 구현하지 않았습니다.");
	}

	@Override
	public void deniedReceipt(long receiptId, long managerId) {
		throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "심사 거절은 아직 구현하지 않았습니다.");
	}

}
