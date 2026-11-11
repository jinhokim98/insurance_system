package com.example.insurancesystem.receipt;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.KeyHolder;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class ReceiptRepositoryTests {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final ReceiptRepository repository = new ReceiptRepositoryImpl(jdbc);
    private final LocalDateTime time = LocalDateTime.of(2026, 11, 10, 13, 45);

    @Test
    void contractQueryBindsIdAndReturnsAllRows() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getLong("id")).thenReturn(10L, 11L);
        when(rs.getLong("contract_id")).thenReturn(3L);
        when(rs.getObject("crash_time", LocalDateTime.class)).thenReturn(time);
        when(rs.getObject("bill_amount", Long.class)).thenReturn(null);
        when(jdbc.query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<Receipt>>any(), eq(3L)))
                .thenAnswer(invocation -> {
                    RowMapper<Receipt> mapper = invocation.getArgument(1);
                    return List.of(mapper.mapRow(rs, 0), mapper.mapRow(rs, 1));
                });
        var receipts = repository.findByContractId(3L);
        assertEquals(2, receipts.size());
        assertEquals(11L, receipts.get(1).getId());
        assertEquals(time, receipts.getFirst().getCrashTime());
        assertNull(receipts.getFirst().getBillAmount());
    }

    @Test
    void missingReceiptIsEmpty() {
        when(jdbc.query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<Receipt>>any(), eq(99L)))
                .thenReturn(List.of());
        assertTrue(repository.findById(99L).isEmpty());
        verify(jdbc).query(contains("WHERE id = ?"), org.mockito.ArgumentMatchers.<RowMapper<Receipt>>any(), eq(99L));
    }

    @Test
    void createAcceptsUnknownAmountAndReturnsGeneratedId() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(statement);
        when(jdbc.update(any(PreparedStatementCreator.class), any(KeyHolder.class))).thenAnswer(invocation -> {
            PreparedStatementCreator creator = invocation.getArgument(0);
            creator.createPreparedStatement(connection);
            KeyHolder holder = invocation.getArgument(1);
            holder.getKeyList().add(Map.of("id", 42L));
            return 1;
        });
        var receipt = repository.createReceipt(new ReceiptRequest(3, 1, time, "장소", "경위", null));
        assertEquals(42L, receipt.getId());
        verify(statement).setLong(1, 3);
        verify(statement).setTimestamp(2, Timestamp.valueOf(time));
        verify(statement).setObject(5, null, Types.BIGINT);
    }

    @Test
    void updateTargetsReceiptAndOriginalContract() {
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        assertEquals(1, repository.updateReceipt(10, new ReceiptRequest(3, 1, time, "장소", "경위", null)));
        verify(jdbc).update(contains("WHERE id = ? AND contract_id = ?"),
                eq(Timestamp.valueOf(time)), eq("장소"), eq("경위"), isNull(), eq(10L), eq(3L));
    }

    @Test
    void deleteReturnsAffectedRows() {
        when(jdbc.update("DELETE FROM receipt WHERE id = ?", 10L)).thenReturn(1);
        assertEquals(1, repository.deleteReceipt(10));
        assertEquals(0, repository.deleteReceipt(99));
    }
}
