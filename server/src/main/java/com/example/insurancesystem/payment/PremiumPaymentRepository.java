package com.example.insurancesystem.payment;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class PremiumPaymentRepository {
    private final JdbcTemplate jdbcTemplate;

    public PremiumPaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<PremiumPayment> findByContractId(long contractId) {
        String sql = """
                SELECT id, contract_id, amount, payment_method, paid_at
                FROM premium_ledger
                WHERE contract_id = ?
                """;

        List<PremiumPayment> payments = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new PremiumPayment(
                        rs.getLong("id"),
                        rs.getLong("contract_id"),
                        rs.getLong("amount"),
                        rs.getString("payment_method"),
                        rs.getObject("paid_at", LocalDateTime.class)
                ),
                contractId
        );

        return payments.stream().findFirst();
    }

    public void save(long contractId, long premium, String paymentMethod) {
        String sql = """
                INSERT INTO premium_ledger
                (contract_id, amount, payment_method)
                VALUES (?, ?, ?);
                """;

        jdbcTemplate.update(sql, contractId, premium, paymentMethod);
    }

}