package com.example.insurancesystem.receipt;

import com.example.insurancesystem.contract.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReceiptServiceTests {
    private final ReceiptRepository receipts = mock(ReceiptRepository.class);
    private final InsuranceContractRepository contracts = mock(InsuranceContractRepository.class);
    private final ReceiptService service = new ReceiptServiceImpl(receipts, contracts);
    private final LocalDateTime start = LocalDateTime.of(2020, 1, 1, 0, 0);
    private final LocalDateTime end = start.plusYears(1);
    private void contract(ContractStatus status) {
        when(contracts.findById(3)).thenReturn(Optional.of(new InsuranceContract(
                3, 1, 1, start, start, end, "11가1111", 10000, 100, status)));
    }
    private ReceiptRequest request(long customer, LocalDateTime time, String place, String detail, Long amount) {
        return new ReceiptRequest(3, customer, time, place, detail, amount);
    }
    private void rejected(ReceiptRequest request, int status) {
        assertEquals(status, assertThrows(ResponseStatusException.class, () -> service.createReceipt(request)).getStatusCode().value());
        verifyNoInteractions(receipts);
    }
    @Test void rejectsMissingOrFutureTime() {
        rejected(request(1, null, "장소", "경위", null), 400);
        rejected(request(1, LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(1), "장소", "경위", null), 400);
    }
    @Test void rejectsBlankPlaceOrDetail() {
        for (String blank : new String[]{null, "", " \n\t"}) {
            rejected(request(1, start, blank, "경위", null), 400);
            rejected(request(1, start, "장소", blank, null), 400);
        }
    }
    @Test void rejectsNegativeAmountAndMissingCustomer() {
        rejected(request(1, start, "장소", "경위", -1L), 400);
        rejected(request(0, start, "장소", "경위", null), 400);
    }
    @Test void rejectsOtherCustomer() {
        contract(ContractStatus.ACTIVE);
        rejected(request(2, start, "장소", "경위", null), 400);
    }
    @Test void rejectsOutsideCoverage() {
        contract(ContractStatus.ACTIVE);
        rejected(request(1, start.minusSeconds(1), "장소", "경위", null), 400);
        rejected(request(1, end.plusSeconds(1), "장소", "경위", null), 400);
    }
    @Test void rejectsMissingAndUnpaidContract() {
        rejected(request(1, start, "장소", "경위", null), 404);
        contract(ContractStatus.PENDING_PAYMENT);
        rejected(request(1, start, "장소", "경위", null), 409);
    }
    @Test void acceptsCoverageBoundariesAndPreservesUnknownAndZeroAmount() {
        contract(ContractStatus.ACTIVE);
        var unknown = request(1, start, "장소", "경위", null);
        var zero = request(1, end, "장소", "경위", 0L);
        service.createReceipt(unknown);
        service.createReceipt(zero);
        verify(receipts).createReceipt(unknown);
        verify(receipts).createReceipt(zero);
    }
}
