package com.example.insurancesystem.product;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class InsuranceProductRepository {
    private final JdbcTemplate jdbcTemplate;

    public InsuranceProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<InsuranceProduct> findAll() {
        String sql = """
                SELECT  id,
                        name,
                        description,
                        coverage_limit,
                        premium,
                        coverage_period_days
                FROM insurance_product
                ORDER BY id DESC
                """;
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new InsuranceProduct(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getLong("coverage_limit"),
                        rs.getLong("premium"),
                        rs.getInt("coverage_period_days")
                )
        );
    }

    public Optional<InsuranceProduct> findById(long id) {
        String sql = """
                SELECT id,
                        name,
                        description,
                        coverage_limit,
                        premium,
                        coverage_period_days
                FROM insurance_product
                WHERE id = ?
                """;

        List<InsuranceProduct> products = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new InsuranceProduct(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getLong("coverage_limit"),
                    rs.getLong("premium"),
                    rs.getInt("coverage_period_days")
                ),
                id
        );

        return products.stream().findFirst();
    }

    public void save(CreateInsuranceProductRequest request) {
        String sql = """
                INSERT INTO insurance_product (
                    name,
                    description,
                    coverage_limit,
                    premium,
                    coverage_period_days
                ) VALUES (?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                request.name(),
                request.description(),
                request.coverageLimit(),
                request.premium(),
                request.coveragePeriodDays()
        );
    }

    public int patch(PatchInsuranceProductRequest request) {
        String sql = """
                UPDATE insurance_product SET name = ? WHERE id = ?;
                """;

        return jdbcTemplate.update(
                sql,
                request.name(),
                request.id()
        );
    }

    public int delete(long id) {
        String sql = """
                DELETE FROM insurance_product WHERE id = ?;
                """;

        return jdbcTemplate.update(
                sql,
                id);
    }
}
