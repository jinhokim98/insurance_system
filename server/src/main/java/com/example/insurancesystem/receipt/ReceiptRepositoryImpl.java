package com.example.insurancesystem.receipt;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ReceiptRepositoryImpl implements ReceiptRepository {
	private final JdbcTemplate jdbcTemplate;

	public ReceiptRepositoryImpl(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private Receipt mapToObject(ResultSet rs, int rowNum) throws SQLException {
		return new Receipt(
				rs.getLong("id"),
				rs.getLong("contract_id"),
				rs.getObject("crash_time", LocalDateTime.class),
				rs.getString("crash_place"),
				rs.getString("crash_detail"),
				rs.getObject("bill_amount", Long.class)
		);
	}

	@Override
	public List<Receipt> findByCustomerId(long customerId) {
		String sql = """
				SELECT r.id, r.contract_id, r.crash_time, r.crash_place,
				       r.crash_detail, r.bill_amount
				FROM receipt r
				JOIN insurance_contract c ON c.id = r.contract_id
				WHERE c.customer_id = ?
				ORDER BY r.id DESC
				""";
		return jdbcTemplate.query(sql, this::mapToObject, customerId);
	}

	@Override
	public List<Receipt> findByContractId(long contractId) {
		String sql = """
				SELECT id, contract_id, crash_time, crash_place, crash_detail, bill_amount
				FROM receipt WHERE contract_id = ? ORDER BY id DESC
				""";
		return jdbcTemplate.query(sql, this::mapToObject, contractId);
	}

	@Override
	public Optional<Receipt> findById(long id) {
		String sql = """
				SELECT id, contract_id, crash_time, crash_place, crash_detail, bill_amount
				FROM receipt WHERE id = ?
				""";
		return jdbcTemplate.query(sql, this::mapToObject, id).stream().findFirst();
	}

	@Override
	public Receipt createReceipt(ReceiptRequest request) {
		String sql = """
				INSERT INTO receipt(contract_id, crash_time, crash_place, crash_detail, bill_amount)
				VALUES(?, ?, ?, ?, ?);
				""";

		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		int rows = jdbcTemplate.update(connection -> {
			PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			statement.setLong(1, request.contractId());
			statement.setTimestamp(2, Timestamp.valueOf(request.crashTime()));
			statement.setString(3, request.crashPlace());
			statement.setString(4, request.crashDetail());
			statement.setObject(5, request.billAmount(), Types.BIGINT);
			return statement;
		}, keyHolder);

		Number generatedId = keyHolder.getKey();
		if (rows != 1 || generatedId == null) {
			throw new IllegalStateException("생성된 접수 id를 확인할 수 없습니다.");
		}

		return new Receipt(generatedId.longValue(), request.contractId(), request.crashTime(), request.crashPlace(), request.crashDetail(), request.billAmount());
	}

	@Override
	public int updateReceipt(long id, ReceiptRequest request) {
		String sql = """
				UPDATE receipt
				SET crash_time = ?, crash_place = ?, crash_detail = ?, bill_amount = ?
				WHERE id = ? AND contract_id = ?
				""";
		return jdbcTemplate.update(sql,
				Timestamp.valueOf(request.crashTime()), request.crashPlace(),
				request.crashDetail(), request.billAmount(), id, request.contractId());
	}

	@Override
	public int deleteReceipt(long id) {
		return jdbcTemplate.update("DELETE FROM receipt WHERE id = ?", id);
	}
}
