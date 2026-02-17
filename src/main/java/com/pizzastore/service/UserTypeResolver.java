package com.pizzastore.service;

import com.pizzastore.model.LoginType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


/**
 * Resolves the type of user attempting to log in based on their email domain.
 *
 * This class has a single responsibility: inspect an email address and return
 * a LoginType. It does not authenticate the user — that is AuthService's job.
 * Keeping this logic separate means routing rules can change independently
 * of authentication logic.
 */
@Service
public class UserTypeResolver {

    /**
     * The company email domain loaded from application.properties.
     * Example entry: company.email.domain=yourcompany.com
     */
    @Value("${company.email.domain}")
    private String companyEmailDomain;

    /**
     * Determines the login type for the given email address.
     *
     * @param email the email address entered on the login screen
     * @return WORKER if the domain matches the company domain,
     *         CUSTOMER if it does not,
     *         UNKNOWN if the email is null or malformed
     */
    public LoginType resolve(String email) {
        if (email == null || !email.contains("@")) {
            return LoginType.UNKNOWN;
        }

        String domain = email.substring(email.indexOf("@") + 1).toLowerCase();

        return domain.equals(companyEmailDomain.toLowerCase())
                ? LoginType.WORKER
                : LoginType.CUSTOMER;
    }
}