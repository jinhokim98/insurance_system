package com.example.insurancesystem.receipt;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class Receipt {
	private Long id;
	private Long contractId;
	private Long managerId;
	private LocalDateTime crashTime;
	private String crashPlace;
	private String crashDetail;
	private Long billAmount;
	private ReceiptStatus receiptStatus;

    public void assignManager(long managerId) {
        if (managerId <= 0) {
            throw new IllegalArgumentException("담당자 ID는 양수여야 합니다.");
        }
        if (this.managerId != null || receiptStatus != ReceiptStatus.RECEIVED) {
            throw new IllegalStateException("미배정된 접수 상태에서만 담당자를 할당할 수 있습니다.");
        }
        this.managerId = managerId;
        this.receiptStatus = ReceiptStatus.REVIEW_START;
    }
}
