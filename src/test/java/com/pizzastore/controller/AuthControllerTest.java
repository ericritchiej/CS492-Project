package com.pizzastore.controller;

import com.pizzastore.model.Employee;
import com.pizzastore.model.LoginType;
import com.pizzastore.model.User;
import com.pizzastore.repository.EmployeeRepository;
import com.pizzastore.repository.UserRepository;
import com.pizzastore.service.UserTypeResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private UserRepository userRepository;
    private EmployeeRepository employeeRepository;
    private UserTypeResolver userTypeResolver;
    private AuthController controller;

    // BCrypt hash of "Pizza123!" — used so passwordEncoder.matches() returns true
    private static final String HASHED_PASSWORD =
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        employeeRepository = mock(EmployeeRepository.class);
        userTypeResolver = mock(UserTypeResolver.class);
        controller = new AuthController(userRepository, userTypeResolver, employeeRepository);
    }

    // --- identify endpoint ---

    @Test
    void identifyReturnsCustomerForCustomerEmail() {
        when(userTypeResolver.resolve("jane@gmail.com")).thenReturn(LoginType.CUSTOMER);

        ResponseEntity<?> response = controller.identify(Map.of("email", "jane@gmail.com"));

        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(LoginType.CUSTOMER, body.get("loginType"));
    }

    @Test
    void identifyReturnsWorkerForWorkerEmail() {
        when(userTypeResolver.resolve("bob@work.com")).thenReturn(LoginType.WORKER);

        ResponseEntity<?> response = controller.identify(Map.of("email", "bob@work.com"));

        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(LoginType.WORKER, body.get("loginType"));
    }

    @Test
    void identifyReturns400ForInvalidEmail() {
        when(userTypeResolver.resolve("bad")).thenReturn(LoginType.UNKNOWN);

        ResponseEntity<?> response = controller.identify(Map.of("email", "bad"));

        assertEquals(400, response.getStatusCodeValue());
    }

    // --- customer signin ---

    @Test
    void customerSigninSucceedsWithValidCredentials() {
        when(userTypeResolver.resolve("jane@gmail.com")).thenReturn(LoginType.CUSTOMER);
        User user = new User();
        user.setId(1L);
        user.setEmail("jane@gmail.com");
        user.setFirstName("Jane");
        user.setLastName("Doe");
        // Use a real BCrypt hash so the encoder can verify it
        user.setPassword(org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
                .class.getName()); // placeholder — we'll use a real hash below
        // Generate a real hash for "Pizza123!"
        String realHash = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                .encode("Pizza123!");
        user.setPassword(realHash);
        when(userRepository.findByUsername("jane@gmail.com")).thenReturn(List.of(user));

        ResponseEntity<?> response = controller.handleCustomerSignIn("jane@gmail.com", "Pizza123!");

        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Login successful", body.get("message"));
        Map<?, ?> userDto = (Map<?, ?>) body.get("user");
        assertEquals("Jane", userDto.get("firstName"));
        assertEquals("Doe", userDto.get("lastName"));
        // Password hash must NOT be in the response
        assertNull(userDto.get("password"));
    }

    @Test
    void customerSigninFailsWithWrongPassword() {
        when(userTypeResolver.resolve("jane@gmail.com")).thenReturn(LoginType.CUSTOMER);
        User user = new User();
        user.setEmail("jane@gmail.com");
        String realHash = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                .encode("Pizza123!");
        user.setPassword(realHash);
        when(userRepository.findByUsername("jane@gmail.com")).thenReturn(List.of(user));

        ResponseEntity<?> response = controller.handleCustomerSignIn("jane@gmail.com", "WrongPass!");

        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void customerSigninFailsForUnknownEmail() {
        when(userTypeResolver.resolve("nobody@gmail.com")).thenReturn(LoginType.CUSTOMER);
        when(userRepository.findByUsername("nobody@gmail.com")).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = controller.handleCustomerSignIn("nobody@gmail.com", "Pizza123!");

        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void customerSigninRejectsWorkerEmail() {
        when(userTypeResolver.resolve("bob@work.com")).thenReturn(LoginType.WORKER);

        ResponseEntity<?> response = controller.handleCustomerSignIn("bob@work.com", "Pizza123!");

        assertEquals(401, response.getStatusCodeValue());
    }

    // --- employee signin ---

    @Test
    void employeeSigninSucceedsWithValidCredentials() {
        when(userTypeResolver.resolve("bob@work.com")).thenReturn(LoginType.WORKER);
        Employee employee = new Employee();
        employee.setEmployeeId(10L);
        employee.setEmail("bob@work.com");
        employee.setFirstName("Bob");
        employee.setLastName("Smith");
        employee.setRole("Manager");
        String realHash = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                .encode("Pizza123!");
        employee.setPassword(realHash);
        when(employeeRepository.findByUsername("bob@work.com")).thenReturn(List.of(employee));

        ResponseEntity<?> response = controller.handleEmployeeSignIn("bob@work.com", "Pizza123!");

        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Employee Login successful", body.get("message"));
        Map<?, ?> userDto = (Map<?, ?>) body.get("user");
        assertEquals("Bob", userDto.get("firstName"));
        assertEquals("Manager", userDto.get("role"));
        assertNull(userDto.get("password"));
    }

    @Test
    void employeeSigninFailsWithWrongPassword() {
        when(userTypeResolver.resolve("bob@work.com")).thenReturn(LoginType.WORKER);
        Employee employee = new Employee();
        employee.setEmail("bob@work.com");
        String realHash = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                .encode("Pizza123!");
        employee.setPassword(realHash);
        when(employeeRepository.findByUsername("bob@work.com")).thenReturn(List.of(employee));

        ResponseEntity<?> response = controller.handleEmployeeSignIn("bob@work.com", "WrongPass!");

        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void employeeSigninRejectsCustomerEmail() {
        when(userTypeResolver.resolve("jane@gmail.com")).thenReturn(LoginType.CUSTOMER);

        ResponseEntity<?> response = controller.handleEmployeeSignIn("jane@gmail.com", "Pizza123!");

        assertEquals(401, response.getStatusCodeValue());
    }

    // --- registration ---

    @Test
    void registerSucceedsForNewCustomer() {
        when(userRepository.findByUsername("new@gmail.com")).thenReturn(Collections.emptyList());
        when(userRepository.createUser(any(User.class), any(com.pizzastore.model.Address.class)))
                .thenReturn(42L);

        // Use reflection to invoke since RegisterRequest is package-private
        ResponseEntity<?> response = controller.handleRegister(
                new RegisterRequest("Jane", "Doe", "555-1234",
                        "123 Main St", "", "Springfield", "IL", "62701",
                        "new@gmail.com", "Pizza123!"));

        assertEquals(201, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Account created successfully.", body.get("message"));
        Map<?, ?> userDto = (Map<?, ?>) body.get("user");
        assertEquals(42L, userDto.get("id"));
        assertEquals("Jane", userDto.get("firstName"));
        assertNull(userDto.get("password"));
    }

    @Test
    void registerFailsForDuplicateEmail() {
        User existing = new User();
        existing.setEmail("taken@gmail.com");
        when(userRepository.findByUsername("taken@gmail.com")).thenReturn(List.of(existing));

        ResponseEntity<?> response = controller.handleRegister(
                new RegisterRequest("Jane", "Doe", "555-1234",
                        "123 Main St", "", "Springfield", "IL", "62701",
                        "taken@gmail.com", "Pizza123!"));

        assertEquals(409, response.getStatusCodeValue());
    }
}