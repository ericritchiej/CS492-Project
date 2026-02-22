package com.pizzastore.controller;

import com.pizzastore.model.Address;
import com.pizzastore.model.Employee;
import com.pizzastore.model.LoginType;
import com.pizzastore.model.User;
import com.pizzastore.repository.EmployeeRepository;
import com.pizzastore.repository.UserRepository;
import com.pizzastore.service.UserTypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record SignInRequest(
            String username,
            String password
    ) {}

    public record RegisterRequest(
            String firstName,
            String lastName,
            String phone,
            String address1,
            String address2,
            String city,
            String state,
            String zip,
            String email,
            String password
    ) {}

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;
    private final UserTypeResolver userTypeResolver;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            UserRepository userRepository,
            UserTypeResolver userTypeResolver,
            EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.userTypeResolver = userTypeResolver;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Called by Angular when a page loads to see if the user is already logged in.
     * We check the session for userId + role that we set during sign-in.
     */
    @GetMapping("/status")
    public Map<String, Object> getStatus(HttpSession session) {
        Map<String, Object> status = new HashMap<>();

        Object userId = session.getAttribute("userId");
        Object role = session.getAttribute("role");
        Object email = session.getAttribute("email");

        boolean loggedIn = (userId != null && role != null);

        status.put("loggedIn", loggedIn);

        if (loggedIn) {
            status.put("message", "User is logged in.");
            status.put("userId", userId);
            status.put("role", role);
            status.put("email", email);
        } else {
            status.put("message", "No user is currently logged in.");
        }

        return status;
    }

    /**
     * Optional: clears the session to "log out" the current user.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Logged out."));
    }

    @PostMapping("/identify")
    public ResponseEntity<?> identify(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        LoginType loginType = userTypeResolver.resolve(email);
        logger.info("loginType = {}", loginType);

        if (loginType == LoginType.UNKNOWN) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "A valid email address is required."
            ));
        }

        return ResponseEntity.ok(Map.of("loginType", loginType));
    }

    @PostMapping("/signIn/customer")
    public ResponseEntity<?> handleCustomerSignIn(@RequestBody SignInRequest request, HttpSession session) {
        String username = request.username();
        String password = request.password();

        logger.info("Sign-in attempt for user: {}", username);

        LoginType loginType = userTypeResolver.resolve(username);
        if (loginType != LoginType.CUSTOMER) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Invalid user type of customer.");
            return ResponseEntity.status(401).body(error);
        }

        List<User> users = userRepository.findByUsername(username);

        if (users != null && !users.isEmpty()) {
            User user = users.get(0);
            logger.info("Found user: {}", username);

            if (passwordEncoder.matches(password, user.getPassword())) {
                logger.info("Password Match");

                session.setAttribute("userId", user.getId());
                session.setAttribute("role", "Customer");
                session.setAttribute("email", user.getEmail());

                return ResponseEntity.ok(buildUserResponse(
                        "Login successful",
                        user.getId(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        "Customer"
                ));
            }
        }

        logger.info("User not found or password incorrect for: {}", username);
        Map<String, String> error = new HashMap<>();
        error.put("message", "Invalid username or password.");
        logger.error("Failed sign-in attempt for: {}", username);

        return ResponseEntity.status(401).body(error);
    }

    @PostMapping("/signIn/employee")
    public ResponseEntity<?> handleEmployeeSignIn(@RequestBody SignInRequest request, HttpSession session) {
        String username = request.username();
        String password = request.password();

        logger.info("Sign-in attempt for employee: {}", username);

        LoginType loginType = userTypeResolver.resolve(username);
        logger.info("Resolved login type: {}", loginType);

        if (loginType != LoginType.WORKER) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Invalid user type for worker.");
            return ResponseEntity.status(401).body(error);
        }

        List<Employee> employees = employeeRepository.findByUsername(username);

        if (employees != null && !employees.isEmpty()) {
            Employee employee = employees.get(0);
            logger.info("Found employee: {}", employee.getEmail());

            if (passwordEncoder.matches(password, employee.getPassword())) {

                session.setAttribute("userId", employee.getEmployeeId());
                session.setAttribute("role", employee.getRole());
                session.setAttribute("email", employee.getEmail());

                return ResponseEntity.ok(buildUserResponse(
                        "Employee Login successful",
                        employee.getEmployeeId(),
                        employee.getEmail(),
                        employee.getFirstName(),
                        employee.getLastName(),
                        employee.getRole()
                ));
            }
        }

        Map<String, String> error = new HashMap<>();
        error.put("message", "Invalid userid or password.");
        return ResponseEntity.status(401).body(error);
    }

    @PostMapping("/register/new/customer")
    public ResponseEntity<?> handleRegister(@RequestBody RegisterRequest request) {
        logger.info("Register attempt for email: {}", request.email());

        List<User> existing = userRepository.findByUsername(request.email());
        if (existing != null && !existing.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "An account with that email already exists.");
            return ResponseEntity.status(409).body(error);
        }

        String hashedPassword = passwordEncoder.encode(request.password());

        User newUser = new User();
        newUser.setEmail(request.email());
        newUser.setFirstName(request.firstName());
        newUser.setLastName(request.lastName());
        newUser.setPhoneNumber(request.phone());
        newUser.setPassword(hashedPassword);

        Address newAddress = new Address();
        newAddress.setAddress1(request.address1());
        newAddress.setAddress2(request.address2());
        newAddress.setCity(request.city());
        newAddress.setState(request.state());
        newAddress.setZip(request.zip());

        Long newCustomerId = userRepository.createUser(newUser, newAddress);

        Map<String, Object> userDto = new HashMap<>();
        userDto.put("id", newCustomerId);
        userDto.put("email", request.email());
        userDto.put("firstName", request.firstName());
        userDto.put("lastName", request.lastName());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Account created successfully.");
        response.put("user", userDto);

        return ResponseEntity.status(201).body(response);
    }

    private Map<String, Object> buildUserResponse(String message, long id, String email, String firstName, String lastName, String role) {
        Map<String, Object> userDto = new HashMap<>();
        userDto.put("id", id);
        userDto.put("email", email);
        userDto.put("firstName", firstName);
        userDto.put("lastName", lastName);
        userDto.put("role", role);

        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("user", userDto);

        return response;
    }
}
