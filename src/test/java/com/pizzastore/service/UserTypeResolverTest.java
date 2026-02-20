package com.pizzastore.service;

import com.pizzastore.model.LoginType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class UserTypeResolverTest {

    private UserTypeResolver resolver;

    @BeforeEach
    void setUp() throws Exception {
        resolver = new UserTypeResolver();
        // Set the private companyEmailDomain field that would normally be
        // injected by Spring from application.properties
        Field field = UserTypeResolver.class.getDeclaredField("companyEmailDomain");
        field.setAccessible(true);
        field.set(resolver, "work.com");
    }

    @Test
    void customerEmailReturnsCustomer() {
        assertEquals(LoginType.CUSTOMER, resolver.resolve("jane@gmail.com"));
    }

    @Test
    void workerEmailReturnsWorker() {
        assertEquals(LoginType.WORKER, resolver.resolve("bob@work.com"));
    }

    @Test
    void workerEmailIsCaseInsensitive() {
        assertEquals(LoginType.WORKER, resolver.resolve("bob@WORK.COM"));
    }

    @Test
    void nullEmailReturnsUnknown() {
        assertEquals(LoginType.UNKNOWN, resolver.resolve(null));
    }

    @Test
    void emailWithoutAtSignReturnsUnknown() {
        assertEquals(LoginType.UNKNOWN, resolver.resolve("noatsign"));
    }

    @Test
    void emptyStringReturnsUnknown() {
        assertEquals(LoginType.UNKNOWN, resolver.resolve(""));
    }
}