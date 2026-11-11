package com.example.insurancesystem.manager;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class ManagerRepositoryImpl implements ManagerRepository {
	private final JdbcTemplate jdbcTemplate;

	public ManagerRepositoryImpl(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public Optional<Manager> findById(long managerId) {
		String sql = """
				SELECT id, department FROM manager WHERE id = ?;
				""";

		List<Manager> managers = jdbcTemplate.query(sql,
				(rs, rowNum) -> new Manager(rs.getLong("id"), rs.getString("department"))
				, managerId);

		return managers.stream().findFirst();
	}

	@Override
	public Manager createManager(String department) {
		String sql = """
				INSERT INTO manager(department) VALUES(?);
				""";

		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		int rows = jdbcTemplate.update(connection -> {
			PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, department);
			return statement;
		}, keyHolder);

		Number generatedId = keyHolder.getKey();
		if (rows != 1 || generatedId == null) {
			throw new IllegalStateException("생성된 매니저 id를 확인할 수 없습니다.");
		}

		return new Manager(generatedId.longValue(), department);
	}
}
