package com.example.insurancesystem.contract;

import com.example.insurancesystem.customer.*;
import com.example.insurancesystem.product.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.KeyHolder;

import java.time.LocalDate;
import java.time.ZoneId;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InsuranceContractCreationTests {
    private final CustomerRepository customers = mock(CustomerRepository.class);
    private final InsuranceProductRepository products = mock(InsuranceProductRepository.class);
    private final InsuranceContractRepository contracts = mock(InsuranceContractRepository.class);
    private final InsuranceContractService service = new InsuranceContractService(customers, products, contracts);
    private final LocalDate start = LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(10);

    private void existingCustomer() {
        when(customers.findById(1L)).thenReturn(Optional.of(new Customer(
                1L, "테스트", LocalDate.of(1990, 1, 1), Gender.MALE, LocalDate.of(2010, 1, 1))));
    }

    @Test
    void copiesProductTermsAndReturnsGeneratedId() {
        existingCustomer();
        when(products.findById(2L)).thenReturn(Optional.of(new InsuranceProduct(
                2L, "상품", "설명", 30000000L, 120000L, 365)));
        when(contracts.create(any())).thenReturn(42L);
        assertEquals(42L, service.createContract(new CreateContractRequest(1, 2, " 11가1111 ", start)));
        ArgumentCaptor<NewInsuranceContract> captor = ArgumentCaptor.forClass(NewInsuranceContract.class);
        verify(contracts).create(captor.capture());
        var contract = captor.getValue();
        assertEquals(30000000L, contract.coverageLimit());
        assertEquals(120000L, contract.premium());
        assertEquals(start.atStartOfDay(), contract.coverageStartsAt());
        assertEquals(start.plusDays(365).atStartOfDay(), contract.coverageEndsAt());
        assertEquals("11가1111", contract.vehicleNumber());
        assertEquals(ContractStatus.PENDING_PAYMENT, contract.status());
    }

    @Test
    void missingCustomerStopsBeforeProductAndSave() {
        when(customers.findById(1L)).thenReturn(Optional.empty());
        var error = assertThrows(ResponseStatusException.class,
                () -> service.createContract(new CreateContractRequest(1, 2, "11가1111", start)));
        assertEquals(404, error.getStatusCode().value());
        verifyNoInteractions(products, contracts);
    }

    @Test
    void missingProductDoesNotSave() {
        existingCustomer();
        when(products.findById(2L)).thenReturn(Optional.empty());
        var error = assertThrows(ResponseStatusException.class,
                () -> service.createContract(new CreateContractRequest(1, 2, "11가1111", start)));
        assertEquals(404, error.getStatusCode().value());
        verifyNoInteractions(contracts);
    }

    @Test
    void invalidInputsNeverReachDatabase() {
        var requests = new CreateContractRequest[] {
                new CreateContractRequest(0, 2, "차량", start),
                new CreateContractRequest(1, -1, "차량", start),
                new CreateContractRequest(1, 2, null, start),
                new CreateContractRequest(1, 2, "  ", start),
                new CreateContractRequest(1, 2, "가".repeat(21), start),
                new CreateContractRequest(1, 2, "차량", null),
                new CreateContractRequest(1, 2, "차량", LocalDate.now(ZoneId.of("Asia/Seoul")))
        };
        for (var request : requests) {
            var error = assertThrows(ResponseStatusException.class, () -> service.createContract(request));
            assertEquals(400, error.getStatusCode().value());
        }
        verifyNoInteractions(customers, products, contracts);
    }

    @Test
    void repositoryBindsAllTermsAndReturnsKeyNotRowCount() throws Exception {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(statement);
        when(jdbc.update(any(PreparedStatementCreator.class), any(KeyHolder.class))).thenAnswer(invocation -> {
            PreparedStatementCreator creator = invocation.getArgument(0);
            assertSame(statement, creator.createPreparedStatement(connection));
            KeyHolder holder = invocation.getArgument(1);
            holder.getKeyList().add(Map.of("id", 42L));
            return 1;
        });
        var contract = new NewInsuranceContract(1, 2, "11가1111", start.atStartOfDay(),
                start.plusDays(365).atStartOfDay(), 30000000, 120000, ContractStatus.PENDING_PAYMENT);
        assertEquals(42L, new InsuranceContractRepository(jdbc).create(contract));
        verify(statement).setLong(1, 1L);
        verify(statement).setLong(2, 2L);
        verify(statement).setString(3, "11가1111");
        verify(statement).setTimestamp(4, Timestamp.valueOf(contract.coverageStartsAt()));
        verify(statement).setTimestamp(5, Timestamp.valueOf(contract.coverageEndsAt()));
        verify(statement).setLong(6, 30000000L);
        verify(statement).setLong(7, 120000L);
        verify(statement).setString(8, "PENDING_PAYMENT");
    }

    @Test
    void postReturnsCreatedWithDetailLocation() {
        var mockedService = mock(InsuranceContractService.class);
        var request = new CreateContractRequest(1, 2, "11가1111", start);
        when(mockedService.createContract(request)).thenReturn(42L);
        var response = new InsuranceContractController(contracts, mockedService).createContract(request);
        assertEquals(201, response.getStatusCode().value());
        assertEquals(42L, response.getBody());
        assertEquals("/api/contracts/42", response.getHeaders().getLocation().toString());
    }
}
