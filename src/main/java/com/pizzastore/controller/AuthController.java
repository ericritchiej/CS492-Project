package com.pizzastore.controller;

import com.pizzastore.model.Address;
import com.pizzastore.model.User;
import com.pizzastore.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A "record" is a simple way to define a class that just holds data.
 * It automatically creates getters for each field, so you don't have to
 * write them yourself. This one represents the data Angular sends us
 * when a new user fills out the registration form.
 *
 * When Angular sends JSON like { "firstName": "Jane", "email": "jane@example.com" },
 * Spring automatically maps it to this record.
 */
record RegisterRequest(
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

/**
 * A "Controller" is the entry point for HTTP requests coming from the frontend (Angular).
 * Think of it like a receptionist — it receives requests, figures out what to do,
 * and sends back a response.
 *
 * @RestController tells Spring this class handles web requests and automatically
 * converts our return values to JSON.
 *
 * @RequestMapping("/api/auth") means every endpoint in this class starts with
 * /api/auth — so our signin URL becomes /api/auth/signin, etc.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * A logger lets us print messages to the server console while the app is running.
     * This is much better than System.out.println() because it includes timestamps,
     * log levels (INFO, ERROR, etc.), and can be configured to write to log files.
     * LoggerFactory.getLogger(AuthController.class) ties the logger to this specific
     * class so we know where the message came from.
     */
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    /**
     * UserRepository is our connection to the database.
     * Rather than writing SQL directly here in the controller, we delegate
     * all database work to the repository. This keeps our code organized —
     * controllers handle HTTP, repositories handle data.
     *
     * "final" means this reference cannot be reassigned after it's set,
     * which helps prevent accidental bugs.
     */
    private final UserRepository userRepository;

    /**
     * PasswordEncoder handles hashing passwords using the BCrypt algorithm.
     * NEVER store plain text passwords in a database — if your database is
     * ever breached, hashed passwords are extremely difficult to reverse.
     * BCrypt is a industry-standard algorithm designed specifically for passwords.
     */
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * This is the constructor — Spring calls this automatically when the app starts up.
     * Spring sees that we need a UserRepository and automatically "injects" one for us.
     * This pattern is called Dependency Injection, and it means we don't have to
     * manually create objects with "new UserRepository()" — Spring manages that for us.
     */
    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * @GetMapping means this method responds to HTTP GET requests.
     * GET requests are used for fetching data without changing anything.
     *
     * This endpoint (/api/auth/status) is called by Angular when a page loads
     * to check if someone is already logged in. Right now it always returns
     * "not logged in" — in a real app this would check a session or token.
     */
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        // HashMap is a key-value data structure. When Spring returns it,
        // it gets automatically converted to JSON like: { "loggedIn": false, "message": "..." }
        Map<String, Object> status = new HashMap<>();
        status.put("loggedIn", false);
        status.put("message", "No user is currently logged in.");
        return status;
    }

    /**
     * @PostMapping means this method responds to HTTP POST requests.
     * POST requests are used when sending data to the server, like form submissions.
     *
     * @RequestParam means the data comes in as URL parameters or form fields,
     * NOT as JSON. For example: /api/auth/signin?username=jane&password=abc
     * This is different from @RequestBody which expects JSON.
     *
     * ResponseEntity<?> lets us control the HTTP status code we send back.
     * The "?" means it can return different types depending on success or failure.
     */
    @PostMapping("/signin")
    public ResponseEntity<?> handleSignIn(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {

        // Log the sign-in attempt so we can monitor activity on the server.
        // Notice we log the username but NOT the password in plain text —
        // passwords should never appear in log files.
        logger.info("Sign-in attempt for user: {}", username);

        // Ask the repository to look up the user by their email address.
        // This returns a List because findByUsername could theoretically find
        // multiple results, even though we expect just one.
        List<User> users = userRepository.findByUsername(username);

        if (users != null && !users.isEmpty()) {
            // Get the first (and should be only) user from the list
            User user = users.get(0);
            logger.info("Found user: {}", username);

            // passwordEncoder.matches() compares the plain text password the user
            // typed with the hashed version stored in the database.
            // We never "decrypt" the hash — instead BCrypt re-hashes the input
            // and checks if it matches. This is the secure way to verify passwords.
            if (passwordEncoder.matches(password, user.getPassword())) {

                // We create a "DTO" (Data Transfer Object) — a trimmed down version
                // of the user that only includes safe, necessary fields.
                // We deliberately exclude the password hash so it's never sent
                // back to the browser, even in hashed form.
                Map<String, Object> userDto = new HashMap<>();
                userDto.put("id", user.getId());
                userDto.put("email", user.getEmail());
                userDto.put("firstName", user.getFirstName());
                userDto.put("lastName", user.getLastName());

                // Build the full response with a message and the user data.
                // ResponseEntity.ok() sends back HTTP status 200, which means "Success".
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Login successful");
                response.put("user", userDto);

                return ResponseEntity.ok(response);
            }
        }

        // If we reach here, either the user wasn't found or the password was wrong.
        // Importantly, we return the SAME error message for both cases ("Invalid username
        // or password"). This is intentional — telling an attacker "that email doesn't
        // exist" gives them useful information. Vague errors are safer.
        logger.info("User not found or password incorrect for: {}", username);
        Map<String, String> error = new HashMap<>();
        error.put("message", "Invalid username or password.");
        logger.error("Failed sign-in attempt for: {}", username);

        // HTTP 401 means "Unauthorized" — the user's credentials were not accepted.
        return ResponseEntity.status(401).body(error);
    }

    /**
     * This endpoint handles new account registration.
     *
     * @RequestBody means Spring will read the JSON body that Angular sends
     * and automatically map it to our RegisterRequest record.
     * This is different from @RequestParam which reads URL parameters.
     */
    @PostMapping("/register")
    public ResponseEntity<?> handleRegister(@RequestBody RegisterRequest request) {
        logger.info("Register attempt for email: {}", request.email());

        // Before creating anything, check if this email is already in use.
        // Allowing duplicate emails would cause login to behave unpredictably
        // since we look users up by email.
        List<User> existing = userRepository.findByUsername(request.email());
        if (existing != null && !existing.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "An account with that email already exists.");
            // HTTP 409 means "Conflict" — the request couldn't be completed
            // because it conflicts with existing data.
            return ResponseEntity.status(409).body(error);
        }

        // Hash the password BEFORE storing it.
        // The encode() method runs BCrypt on the plain text password,
        // producing a scrambled string like "$2a$10$..." that cannot be reversed.
        String hashedPassword = passwordEncoder.encode(request.password());

        // Build the User object that will be inserted into the customers table.
        // We use the hashed password here, never the plain text version.
        User newUser = new User();
        newUser.setEmail(request.email());
        newUser.setFirstName(request.firstName());
        newUser.setLastName(request.lastName());
        newUser.setPhoneNumber(request.phone());
        newUser.setPassword(hashedPassword);

        // Build the Address object that will be inserted into the addresses table.
        // The address is stored separately so that in the future a customer
        // could have multiple addresses (home, work, etc.).
        Address newAddress = new Address();
        newAddress.setAddress1(request.address1());
        newAddress.setAddress2(request.address2());
        newAddress.setCity(request.city());
        newAddress.setState(request.state());
        newAddress.setZip(request.zip());

        // Save both the user and address to the database.
        // createUser() returns the new customer's ID so we can include it
        // in the response — Angular needs this ID to identify the user.
        Long newCustomerId = userRepository.createUser(newUser, newAddress);

        // Return a trimmed user object (DTO) back to Angular so it can
        // immediately log the user in without requiring them to sign in again.
        // Again, we deliberately exclude the password hash from the response.
        Map<String, Object> userDto = new HashMap<>();
        userDto.put("id", newCustomerId);
        userDto.put("email", request.email());
        userDto.put("firstName", request.firstName());
        userDto.put("lastName", request.lastName());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Account created successfully.");
        response.put("user", userDto);

        // HTTP 201 means "Created" — more specific than 200 and signals that
        // a new resource was successfully created on the server.
        return ResponseEntity.status(201).body(response);
    }
}