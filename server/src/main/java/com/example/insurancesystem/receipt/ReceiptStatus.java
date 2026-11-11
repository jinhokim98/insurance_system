package com.example.insurancesystem.receipt;

public enum ReceiptStatus {
	RECEIVED, // 고객 접수
	REVIEW_START, // 담당자가 확인하고 심사 시작
	REVIEW_COMPLETED, // 심사 완료 후 지급 전
	PAYMENT_COMPLETED // 지급 완료
}
