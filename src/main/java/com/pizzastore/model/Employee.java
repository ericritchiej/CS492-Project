package com.pizzastore.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * This is a "Model" class — it represents a row in the "employees" database table.
 * Think of it as a blueprint that describes what a Employee looks like in our system.
 *
 * This class is used to read from the database — when we look up a Employee to verify their login,
 *      jOOQ maps the database row into a Employee object so we can work with it in Java
 *
 * Lombok Annotations automatically generate repetitive boilerplate code:
 *   @Data            — generates getters (getEmail(), getFirstName(), etc.),
 *                      setters (setEmail(), setFirstName(), etc.),
 *                      equals(), hashCode(), and toString()
 *   @NoArgsConstructor — generates an empty constructor: new Employee()
 *                        this is required by Spring and jOOQ when they create
 *                        Employee objects while reading data from the database
 *   @AllArgsConstructor — generates a constructor with all fields as parameters:
 *                         new Employee(id, email, firstName, lastName, phoneNumber, password)
 */

/**
 * @Entity tells Spring/JPA that this class maps to a database table.
 *
 * @Table(name = "employees") tells Spring the exact table name in the database.
 * Our class is called "Employee" but the table is called "employees" — this annotation
 * bridges that gap so Spring knows where to look.
 */
@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    /**
     * @Id marks this as the primary key — the unique identifier for each employee.
     * No two employees can ever have the same id.
     *
     * @GeneratedValue(strategy = GenerationType.IDENTITY) means we never set this
     * ourselves — the database automatically assigns the next available number
     * when a new employee row is inserted. In PostgreSQL this is handled by
     * a SERIAL or auto-increment column (in our case, employee_id).
     *
     * Note: this field maps to the "employee_id" column in the database, but since
     * we're using jOOQ for our queries rather than JPA, the @Id annotation is mainly
     * here for documentation purposes to signal that this is the primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long employeeId;

    /**
     * The employee's email address. We use this as their user id for login,
     * which is why EmployeeRepository.findByUserId() searches by email.
     * Email makes a good user id because it's unique to each person and
     * they're unlikely to forget it.
     *
     * @Column(name = "email") maps this Java field to the "email" column.
     * In this case the names match, but we include it for clarity.
     */
    @Column(name = "email")
    private String email;

    /**
     * We store first and last name separately rather than as one "full name" field.
     * This makes it easier to:
     *   - Display just the first name (e.g. "Welcome back, Jane!")
     *   - Sort employees alphabetically by last name
     *   - Format names differently for different purposes
     *
     * @Column(name = "first_name") maps to the "first_name" column in the database.
     * The @Column annotation is necessary here because Java convention uses
     * camelCase (firstName) while databases typically use snake_case (first_name).
     */
    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    /**
     * IMPORTANT: Despite being named "password" in Java, this field stores
     * the HASHED version of the password, never the plain text.
     *
     * The database column is named "password_hash" to make this crystal clear.
     * When a Employee registers, their password goes through BCrypt hashing in
     * AuthController before being stored here — the original password is
     * never saved anywhere.
     *
     * When a Employee logs in, we use passwordEncoder.matches() to compare
     * what they typed against this hash — we never "decrypt" the hash.
     */
    @Column(name = "password_hash")
    private String password;

    @Column(name = "role")
    private String role;
}