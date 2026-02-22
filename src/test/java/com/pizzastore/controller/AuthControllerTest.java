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
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private UserRepository userRepository;
    private EmployeeRepository employeeRepository;
    private UserTypeResolver userTypeResolver;
    private PasswordEncoder passwordEncoder;
    private AuthController controller;

    // BCrypt hash of "Pizza123!" — used so passwordEncoder.matches() returns true
    private static final String HASHED_PASSWORD =
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        employeeRepository = mock(EmployeeRepository.class);
        userTypeResolver = mock(UserTypeResolver.class);
        passwordEncoder = mock(PasswordEncoder.class);
        controller = new AuthController(userRepository, userTypeResolver, employeeRepository, passwordEncoder);
    }

    // --- identify endpoint ---

    @Test
    @SuppressWarnings("ConstantConditions")
    void identifyReturnsCustomerForCustomerEmail() {
        when(userTypeResolver.resolve("jane@gmail.com")).thenReturn(LoginType.CUSTOMER);

        ResponseEntity<?> response = controller.identify(Map.of("email", "jane@gmail.com"));

        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(LoginType.CUSTOMER, body.get("loginType"));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
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
    @SuppressWarnings("ConstantConditions")
    void customerSigninSucceedsWithValidCredentials() {
        HttpSession session = mock(HttpSession.class);

        when(userTypeResolver.resolve("jane@gmail.com")).thenReturn(LoginType.CUSTOMER);

        User user = new User();
        user.setId(1L);
        user.setEmail("jane@gmail.com");
        user.setFirstName("Jane");
        user.setLastName("Doe");

        // We don't need a real BCrypt hash here since passwordEncoder is mocked.
        String storedHash = "fakeHash";
        user.setPassword(storedHash);

        when(userRepository.findByUsername("jane@gmail.com")).thenReturn(List.of(user));
        when(passwordEncoder.matches("Pizza123!", storedHash)).thenReturn(true);

        ResponseEntity<?> response = controller.handleCustomerSignIn(
                new AuthController.SignInRequest("jane@gmail.com", "Pizza123!"),
                session
        );

        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Login successful", body.get("message"));

        Map<?, ?> userDto = (Map<?, ?>) body.get("user");
        assertEquals("Jane", userDto.get("firstName"));
        assertEquals("Doe", userDto.get("lastName"));
        assertNull(userDto.get("password"));

        // Optional: verify session was populated
        verify(session).setAttribute("userId", 1L);
        verify(session).setAttribute("role", "Customer");
        verify(session).setAttribute("email", "jane@gmail.com");
    }

    @Test
    void customerSigninFailsWithWrongPassword() {
        HttpSession session = mock(HttpSession.class);

        when(userTypeResolver.resolve("jane@gmail.com")).thenReturn(LoginType.CUSTOMER);

        User user = new User();
        user.setEmail("jane@gmail.com");
        String storedHash = "fakeHash";
        user.setPassword(storedHash);

        when(userRepository.findByUsername("jane@gmail.com")).thenReturn(List.of(user));
        when(passwordEncoder.matches("WrongPass!", storedHash)).thenReturn(false);

        ResponseEntity<?> response = controller.handleCustomerSignIn(
                new AuthController.SignInRequest("jane@gmail.com", "WrongPass!"),
                session
        );

        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void customerSigninFailsForUnknownEmail() {
        HttpSession session = mock(HttpSession.class);

        when(userTypeResolver.resolve("nobody@gmail.com")).thenReturn(LoginType.CUSTOMER);
        when(userRepository.findByUsername("nobody@gmail.com")).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = controller.handleCustomerSignIn(
                new AuthController.SignInRequest("nobody@gmail.com", "Pizza123!"),
                session
        );

        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void customerSigninRejectsWorkerEmail() {
        HttpSession session = mock(HttpSession.class);

        when(userTypeResolver.resolve("bob@work.com")).thenReturn(LoginType.WORKER);

        ResponseEntity<?> response = controller.handleCustomerSignIn(
                new AuthController.SignInRequest("bob@work.com", "Pizza123!"),
                session
        );

        assertEquals(401, response.getStatusCodeValue());
    }

    // --- employee signin ---

    @Test
    @SuppressWarnings("ConstantConditions")
    void employeeSigninSucceedsWithValidCredentials() {
        HttpSession session = mock(HttpSession.class);

        when(userTypeResolver.resolve("bob@work.com")).thenReturn(LoginType.WORKER);

        Employee employee = new Employee();
        employee.setEmployeeId(10L);
        employee.setEmail("bob@work.com");
        employee.setFirstName("Bob");
        employee.setLastName("Smith");
        employee.setRole("Manager");

        String storedHash = "fakeHash";
        employee.setPassword(storedHash);

        when(employeeRepository.findByUsername("bob@work.com")).thenReturn(List.of(employee));
        when(passwordEncoder.matches("Pizza123!", storedHash)).thenReturn(true);

        ResponseEntity<?> response = controller.handleEmployeeSignIn(
                new AuthController.SignInRequest("bob@work.com", "Pizza123!"),
                session
        );

        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Employee Login successful", body.get("message"));

        Map<?, ?> userDto = (Map<?, ?>) body.get("user");
        assertEquals("Bob", userDto.get("firstName"));
        assertEquals("Manager", userDto.get("role"));
        assertNull(userDto.get("password"));

        // Optional: verify session was populated
        verify(session).setAttribute("userId", 10L);
        verify(session).setAttribute("role", "Manager");
        verify(session).setAttribute("email", "bob@work.com");
    }

    @Test
    void employeeSigninFailsWithWrongPassword() {
        HttpSession session = mock(HttpSession.class);

        when(userTypeResolver.resolve("bob@work.com")).thenReturn(LoginType.WORKER);

        Employee employee = new Employee();
        employee.setEmail("bob@work.com");
        String storedHash = "fakeHash";
        employee.setPassword(storedHash);

        when(employeeRepository.findByUsername("bob@work.com")).thenReturn(List.of(employee));
        when(passwordEncoder.matches("WrongPass!", storedHash)).thenReturn(false);

        ResponseEntity<?> response = controller.handleEmployeeSignIn(
                new AuthController.SignInRequest("bob@work.com", "WrongPass!"),
                session
        );

        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void employeeSigninRejectsCustomerEmail() {
        HttpSession session = mock(HttpSession.class);

        when(userTypeResolver.resolve("jane@gmail.com")).thenReturn(LoginType.CUSTOMER);

        ResponseEntity<?> response = controller.handleEmployeeSignIn(
                new AuthController.SignInRequest("jane@gmail.com", "Pizza123!"),
                session
        );

        assertEquals(401, response.getStatusCodeValue());
    }

    // --- registration ---

    @Test
    @SuppressWarnings("ConstantConditions")
    void registerSucceedsForNewCustomer() {
        when(userRepository.findByUsername("new@gmail.com")).thenReturn(Collections.emptyList());
        when(passwordEncoder.encode("Pizza123!")).thenReturn(HASHED_PASSWORD);
        when(userRepository.createUser(any(User.class), any(com.pizzastore.model.Address.class)))
                .thenReturn(42L);

        ResponseEntity<?> response = controller.handleRegister(
                new AuthController.RegisterRequest("Jane", "Doe", "555-1234",
                        "123 Main St", "", "Springfield", "IL", "62701",
                        "new@gmail.com", "Pizza123!")
        );

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
                new AuthController.RegisterRequest("Jane", "Doe", "555-1234",
                        "123 Main St", "", "Springfield", "IL", "62701",
                        "taken@gmail.com", "Pizza123!")
        );

        assertEquals(409, response.getStatusCodeValue());
    }
}
