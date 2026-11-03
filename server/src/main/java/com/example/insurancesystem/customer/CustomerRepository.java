package com.example.insurancesystem.customer;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class CustomerRepository {
    private final JdbcTemplate jdbcTemplate;

    public CustomerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Customer> findAll() {
        String sql = "SELECT id, name, birth_date, gender, license_issued_date FROM customer ORDER BY id ASC";
        List<Customer> customers = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new Customer(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getObject("birth_date", LocalDate.class),
                        Gender.valueOf(rs.getString("gender")),
                        rs.getObject("license_issued_date", LocalDate.class)
                )
        );

        return customers;
    }

    public Optional<Customer> findById(Long id) {
        String sql = """
                SELECT id, name, birth_date, gender, license_issued_date
                FROM customer
                WHERE id = ?
                """;

        List<Customer> customers = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new Customer(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getObject("birth_date", LocalDate.class),
                        Gender.valueOf(rs.getString("gender")),
                        rs.getObject("license_issued_date", LocalDate.class)
                ),
                id
        );

        return customers.stream().findFirst();
    }
}
