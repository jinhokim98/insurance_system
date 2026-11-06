package com.example.insurancesystem.contract;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;

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

    public long create(NewInsuranceContract contract) {
        String sql = """
                INSERT INTO insurance_contract (
                    customer_id, product_id, vehicle_number,
                    coverage_starts_at, coverage_ends_at,
                    coverage_limit, premium, status
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        int rows = jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, contract.customerId());
            statement.setLong(2, contract.productId());
            statement.setString(3, contract.vehicleNumber());
            statement.setTimestamp(4, Timestamp.valueOf(contract.coverageStartsAt()));
            statement.setTimestamp(5, Timestamp.valueOf(contract.coverageEndsAt()));
            statement.setLong(6, contract.coverageLimit());
            statement.setLong(7, contract.premium());
            statement.setString(8, contract.status().name());
            return statement;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (rows != 1 || generatedId == null) {
            throw new IllegalStateException("생성된 계약 ID를 확인할 수 없습니다.");
        }
        return generatedId.longValue();
    }

    public boolean existsOverlapping(
            String vehicleNumber,
            LocalDateTime startsAt,
            LocalDateTime endsAt
    ) {
        String sql = """
                SELECT COUNT(*)
                FROM insurance_contract
                WHERE vehicle_number = ?
                  AND status IN ('PENDING_PAYMENT', 'ACTIVE')
                  AND coverage_starts_at < ?
                  AND coverage_ends_at > ?
                """;

        Long count = jdbcTemplate.queryForObject(
                sql,
                Long.class,
                vehicleNumber,
                endsAt,
                startsAt
        );

        return count != null && count > 0;
    }
    
    public int activate(long contractId) {
        String sql = """
                        UPDATE insurance_contract
                        SET status = 'ACTIVE'
                        WHERE id = ? AND status = 'PENDING_PAYMENT';
                """;

        return jdbcTemplate.update(sql, contractId);
    }
}
