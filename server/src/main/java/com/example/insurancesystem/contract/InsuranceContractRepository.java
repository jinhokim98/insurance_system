package com.example.insurancesystem.contract;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class InsuranceContractRepository {
    private final JdbcTemplate jdbcTemplate;

    public InsuranceContractRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<InsuranceContract> findByCustomerId(long customerId) {
        String sql = """
                SELECT id, customer_id, product_id, created_at,
                       coverage_starts_at, coverage_ends_at, vehicle_number,
                       coverage_limit, premium, status
                FROM insurance_contract
                WHERE customer_id = ?
                ORDER BY id DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapContract(rs),
                customerId
        );
    }

    public Optional<InsuranceContract> findById(long id) {
        String sql = """
                SELECT id, customer_id, product_id, created_at,
                       coverage_starts_at, coverage_ends_at, vehicle_number,
                       coverage_limit, premium, status
                FROM insurance_contract
                WHERE id = ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapContract(rs), id)
                .stream().findFirst();
    }

    private InsuranceContract mapContract(ResultSet rs) throws SQLException {
        return new InsuranceContract(
                rs.getLong("id"),
                rs.getLong("customer_id"),
                rs.getLong("product_id"),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getObject("coverage_starts_at", LocalDateTime.class),
                rs.getObject("coverage_ends_at", LocalDateTime.class),
                rs.getString("vehicle_number"),
                rs.getLong("coverage_limit"),
                rs.getLong("premium"),
                ContractStatus.valueOf(rs.getString("status"))
        );
    }
}
