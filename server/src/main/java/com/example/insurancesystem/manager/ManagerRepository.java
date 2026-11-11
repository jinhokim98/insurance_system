package com.example.insurancesystem.manager;

import java.util.Optional;

public interface ManagerRepository {
	Optional<Manager> findById(long managerId);

	Manager createManager(String department);

	Optional<Manager> findSpareMember();
}
