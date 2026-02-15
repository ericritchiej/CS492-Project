import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * This class sets up shared objects that the rest of the app can use.
 *
 * @Configuration tells Spring "this class contains setup instructions."
 * Spring reads it at startup and follows those instructions to prepare
 * the app before any requests come in.
 *
 * Think of this file as a factory — it creates tools that other parts
 * of the app can request and use without having to build them themselves.
 */
@Configuration
public class SecurityBeans {

    /**
     * This method creates and provides a PasswordEncoder for the entire application.
     *
     * @Bean tells Spring to call this method at startup and store the result.
     * Any class that needs a PasswordEncoder (like AuthController) can simply
     * declare it as a dependency and Spring will hand them this exact instance.
     * This is called "Dependency Injection" — classes don't create their own
     * tools, they ask Spring for them.
     *
     * WHY A SHARED BEAN?
     * Without this, every class that needs password hashing would create its own
     * BCryptPasswordEncoder with "new BCryptPasswordEncoder()". That works, but
     * having one shared instance is cleaner and more memory efficient.
     *
     * WHY BCrypt?
     * BCrypt is an algorithm specifically designed for hashing passwords. It has
     * two important properties that make it ideal for security:
     *
     *   1. ONE-WAY — once a password is hashed, it cannot be reversed back to
     *      the original. Even we (the developers) can never see a user's password
     *      after they set it.
     *
     *   2. SLOW BY DESIGN — BCrypt is intentionally slow to compute. This doesn't
     *      matter for normal use (a few milliseconds per login), but it makes
     *      brute force attacks (trying millions of passwords) extremely time consuming
     *      for attackers.
     *
     * HOW TO USE IT:
     *
     *   Hashing a password (at registration):
     *     user.setPassword(passwordEncoder.encode(rawPassword));
     *     This converts "myPassword123" → "$2a$10$xyz..." (a scrambled hash string)
     *     Store the hash in the database, never the original password.
     *
     *   Verifying a password (at login):
     *     passwordEncoder.matches(rawPassword, user.getPassword());
     *     This returns true if the plain text input matches the stored hash,
     *     false if it doesn't. You never decrypt the hash — BCrypt re-hashes
     *     the input and compares the results.
     *
     * PasswordEncoder is the interface (the general type).
     * BCryptPasswordEncoder is the implementation (the specific algorithm).
     * Returning the interface type rather than the specific class means we could
     * swap BCrypt for a different algorithm in the future by only changing this
     * one file — nothing else in the app would need to change.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}