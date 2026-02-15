package com.pizzastore.repository;

import com.pizzastore.model.User;
import com.pizzastore.model.Address;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.Select;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A "Repository" is responsible for all communication with the database.
 * Think of it as the only class that is allowed to speak "database language."
 *
 * This separation is intentional and is called the "Repository Pattern":
 *   - Controllers (AuthController) handle HTTP requests and responses
 *   - Repositories (UserRepository) handle database reads and writes
 *   - Models (User, Address) represent the data itself
 *
 * If we ever switch databases (e.g. from PostgreSQL to MySQL), we only
 * need to change code in this file — nothing else needs to know.
 *
 * @Repository tells Spring this is a repository class. Spring will:
 *   1. Automatically create an instance of it at startup
 *   2. Make it available for injection into controllers
 *   3. Translate database exceptions into Spring's standard exception types
 */
@Repository
public class UserRepository {

    /**
     * DSLContext is jOOQ's main object for building and executing SQL queries.
     * DSL stands for "Domain Specific Language" — jOOQ lets us write SQL-like
     * code in Java instead of raw SQL strings, which reduces typos and makes
     * queries easier to read and maintain.
     *
     * Spring automatically creates and injects this for us based on the
     * database connection settings in application.properties.
     */
    private final DSLContext dsl;

    /**
     * Logger for printing messages to the server console.
     * Useful for monitoring what the app is doing and debugging problems.
     * LoggerFactory.getLogger(UserRepository.class) tags all messages from
     * this class so we know exactly where they came from in the logs.
     */
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

    /**
     * Constructor — Spring calls this automatically at startup and injects
     * the DSLContext for us. This is called "Dependency Injection."
     * We store it in a field so all methods in this class can use it.
     */
    public UserRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * Looks up a customer in the database by their email address.
     * We use email as the "username" for login purposes.
     *
     * Returns a List<User> rather than a single User because the database
     * query could theoretically return multiple rows. In practice we expect
     * zero (user not found) or one (user found), but using a List lets us
     * handle both cases safely without exceptions.
     *
     * @param username  the email address to search for
     * @return          a list of matching users (usually empty or one item)
     */
    public List<User> findByUsername(String username) {
        logger.info("Finding users by username {}", username);

        // Build the SELECT query using jOOQ.
        // This is equivalent to the following SQL:
        //   SELECT email, first_name, last_name, phone_number, password_hash
        //   FROM customers
        //   WHERE email = ?
        //
        // We list each column explicitly rather than using SELECT * for two reasons:
        //   1. We only fetch the data we actually need, which is faster
        //   2. It makes the code self-documenting — you can see exactly what fields
        //      are being returned without looking at the database schema
        //
        // We define the query here but don't execute it yet — this lets us
        // log the SQL before running it, which is helpful for debugging.
        Select<?> query = dsl.select(
                        DSL.field("email", String.class),
                        // .as("first_name") ensures jOOQ maps this column to the
                        // "firstName" field on the User class (via the @Column annotation)
                        DSL.field("first_name", String.class).as("first_name"),
                        DSL.field("last_name", String.class).as("last_name"),
                        DSL.field("phone_number", String.class).as("phone_number"),
                        DSL.field("password_hash", String.class).as("password_hash")
                )
                .from(DSL.table("customers"))
                // .eq() means "equals" — this is the WHERE clause
                // jOOQ uses ? placeholders internally to prevent SQL injection attacks,
                // which is where a malicious user tries to sneak SQL code into an input field
                .where(DSL.field("email").eq(username));

        // Log the SQL with placeholders (safe to log — no real values exposed)
        // Example output: "SELECT email, first_name ... WHERE email = ?"
        logger.info("SQL: " + query.getSQL());

        // Log the SQL with actual values filled in — useful for debugging
        // but should be removed or disabled in production since it exposes
        // the actual email address being searched
        logger.info("Full SQL: " + dsl.renderInlined(query));

        // Execute the query and map each row of results into a User object.
        // fetchInto(User.class) tells jOOQ to automatically match column names
        // to fields on the User class using the @Column annotations we defined.
        return query.fetchInto(User.class);
    }

    /**
     * Creates a new customer and their address in the database.
     *
     * This method performs TWO database inserts in sequence:
     *   1. Insert into "customers" table and get back the new customer's ID
     *   2. Insert into "addresses" table using that ID to link the records
     *
     * The address must be inserted second because it needs the customer_id
     * from step 1 — this relationship between tables is called a "foreign key."
     *
     * @param user      the User object containing customer details
     * @param address   the Address object containing address details
     * @return          the newly generated customer_id from the database
     */
    public Long createUser(User user, Address address) {
        logger.info("Creating new user with email {}", user.getEmail());

        // Insert the new customer row into the "customers" table.
        // This is equivalent to the following SQL:
        //   INSERT INTO customers (email, first_name, last_name, phone_number, password_hash)
        //   VALUES (?, ?, ?, ?, ?)
        //   RETURNING customer_id
        //
        // .returning() asks PostgreSQL to send back the value of customer_id
        // after the insert — we need this ID to link the address to this customer.
        // Without RETURNING we'd have to run a second query to find the new ID.
        //
        // Note: user.getPassword() already contains the HASHED password at this point —
        // the hashing was done in AuthController before calling this method.
        Long newCustomerId = dsl.insertInto(DSL.table("customers"))
                .set(DSL.field("email"), user.getEmail())
                .set(DSL.field("first_name"), user.getFirstName())
                .set(DSL.field("last_name"), user.getLastName())
                .set(DSL.field("phone_number"), user.getPhoneNumber())
                .set(DSL.field("password_hash"), user.getPassword())
                .returning(DSL.field("customer_id"))
                .fetchOne()
                // PostgreSQL returns integer IDs as Integer (32-bit) not Long (64-bit).
                // We fetch it as Integer first, then convert to Long with .longValue()
                // so the rest of the app can use it consistently as a Long.
                .get(DSL.field("customer_id", Integer.class))
                .longValue();

        logger.info("Created customer with ID {}", newCustomerId);

        // Now insert the address, linked to the customer we just created.
        // This is equivalent to the following SQL:
        //   INSERT INTO addresses (customer_id, street_addr_1, street_addr_2, city, state, zip_code)
        //   VALUES (?, ?, ?, ?, ?, ?)
        //
        // customer_id here is the foreign key — it's what connects this address
        // row back to the customer row in the customers table.
        // This is how relational databases link data across tables.
        dsl.insertInto(DSL.table("addresses"))
                .set(DSL.field("customer_id"), newCustomerId)
                .set(DSL.field("street_addr_1"), address.getAddress1())
                .set(DSL.field("street_addr_2"), address.getAddress2())
                .set(DSL.field("city"), address.getCity())
                .set(DSL.field("state"), address.getState())
                .set(DSL.field("zip_code"), address.getZip())
                // .execute() runs the INSERT but doesn't return any data —
                // we don't need the address_id back so we just execute and move on
                .execute();

        // Return the new customer ID so AuthController can include it
        // in the response sent back to Angular
        return newCustomerId;
    }
}