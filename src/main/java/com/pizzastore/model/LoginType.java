package com.pizzastore.model;

/**
 * Represents the type of user attempting to log in.
 * This enum is used by UserTypeResolver to classify an incoming login
 * attempt based on the email domain, and by AuthController to route
 * the request to the appropriate authentication flow.
 */
public enum LoginType {

    /** Email domain matches the configured company domain — route to worker auth. */
    WORKER,

    /** Email domain is not a recognised company domain — route to customer auth. */
    CUSTOMER,

    /** Email was null, malformed, or could not be resolved. */
    UNKNOWN
}