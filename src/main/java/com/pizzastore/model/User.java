package com.pizzastore.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;

/*
  This is a "Model" class — it represents a row in the "customers" database table.
  Think of it as a blueprint that describes what a user looks like in our system.

  This class is used in two directions:
    1. Reading from the database — when we look up a user to verify their login,
       jOOQ maps the database row into a User object so we can work with it in Java
    2. Writing to the database — when we register a new user, we populate a User
       object and pass it to the repository to be inserted

  Lombok Annotations automatically generate repetitive boilerplate code:
    @Data            — generates getters (getEmail(), getFirstName(), etc.),
                       setters (setEmail(), setFirstName(), etc.),
                       equals(), hashCode(), and toString()
    @NoArgsConstructor — generates an empty constructor: new User()
                         this is required by Spring and jOOQ when they create
                         User objects while reading data from the database
    @AllArgsConstructor — generates a constructor with all fields as parameters:
                          new User(id, email, firstName, lastName, phoneNumber, password)
 */

/**
 * "@Entity" tells Spring/JPA that this class maps to a database table.
 * "@Table"(name = "customers") tells Spring the exact table name in the database.
 * Our class is called "User" but the table is called "customers" — this annotation
 * bridges that gap so Spring knows where to look.
 */
@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * "@Id" marks this as the primary key — the unique identifier for each customer.
     * No two customers can ever have the same id.
     * "@GeneratedValue"(strategy = GenerationType.IDENTITY) means we never set this
     * ourselves — the database automatically assigns the next available number
     * when a new customer row is inserted. In PostgreSQL this is handled by
     * a SERIAL or auto-increment column (in our case, customer_id).
     * Note: this field maps to the "customer_id" column in the database, but since
     * we're using jOOQ for our queries rather than JPA, the @Id annotation is mainly
     * here for documentation purposes to signal that this is the primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long id;

    /**
     * The customer's email address. We use this as their username for login,
     * which is why UserRepository.findByUsername() searches by email.
     * Email makes a good username because it's unique to each person and
     * they're unlikely to forget it.
     * "@Column"(name = "email") maps this Java field to the "email" column.
     * In this case the names match, but we include it for clarity.
     */
    @Column(name = "email")
    private String email;

    /**
     * We store first and last name separately rather than as one "full name" field.
     * This makes it easier to:
     *   - Display just the first name (e.g. "Welcome back, Jane!")
     *   - Sort customers alphabetically by last name
     *   - Format names differently for different purposes
     * "@Column"(name = "first_name") maps to the "first_name" column in the database.
     * The @Column annotation is necessary here because Java convention uses
     * camelCase (firstName) while databases typically use snake_case (first_name).
     */
    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    /**
     * The customer's phone number stored as a String rather than a number.
     * This is intentional — phone numbers can start with 0, contain dashes,
     * spaces, or parentheses (e.g. "(555) 555-5555"), and we never do
     * math on them, so a String is the correct type.
     */
    @Column(name = "phone_number")
    private String phoneNumber;

    /**
     * IMPORTANT: Despite being named "password" in Java, this field stores
     * the HASHED version of the password, never the plain text.
     * The database column is named "password_hash" to make this crystal clear.
     * When a user registers, their password goes through BCrypt hashing in
     * AuthController before being stored here — the original password is
     * never saved anywhere.
     * When a user logs in, we use passwordEncoder.matches() to compare
     * what they typed against this hash — we never "decrypt" the hash.
     */
    @Column(name = "password_hash")
    private String password;
}