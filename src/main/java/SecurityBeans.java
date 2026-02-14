import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// user.setPassword(passwordEncoder.encode(rawPassword)); = encodes text to hash value
// passwordEncoder.matches(password, user.getPassword()) = when needing to compare an input vs the database

@Configuration
public class SecurityBeans {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
