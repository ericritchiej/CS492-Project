package com.pizzastore.repository;

import com.pizzastore.model.User;
import com.pizzastore.model.Address;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A "Repository" is responsible for all communication with the database.
 * Think of it as the only class that is allowed to speak "database language."
 * This separation is intentional and is called the "Repository Pattern":
 *   - Controllers (AuthController) handle HTTP requests and responses
 *   - Repositories (UserRepository) handle database reads and writes
 *   - Models (User, Address) represent the data itself
 * If we ever switch databases (e.g. from PostgreSQL to MySQL), we only
 * need to change code in this file — nothing else needs to know.
 * "@Repository" tells Spring this is a repository class. Spring will:
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
     * Returns a List<User> rather than a single User because the database
     * query could theoretically return multiple rows. In practice we expect
     * zero (user not found) or one (user found), but using a List lets us
     * handle both cases safely without exceptions.
     *
     * @param username  the email address to search for
     * @return          a list of matching users (usually empty or one item)
     */
    @SuppressWarnings("resource")
    public List<User> findByUsername(String username) {
        logger.info("Finding users by username {}", username);

        // Build the SELECT query using jOOQ.
        // This is equivalent to the following SQL:
        //   SELECT email, first_name, last_name, phone_number, password_hash
        //   FROM customers
        //   WHERE email = ?
        //
        return dsl.select(
                        DSL.field("customer_id"),
                        DSL.field("email"),
                        DSL.field("first_name").as("first_name"),
                        DSL.field("last_name").as("last_name"),
                        DSL.field("phone_number").as("phone_number"),
                        DSL.field("password_hash").as("password_hash")
                )
                .from(DSL.table("customers"))
                .where(DSL.field("email").eq(username))
                .fetchInto(User.class);
    }

    /**
     * Creates a new customer and their address in the database.
     * This method performs TWO database inserts in sequence:
     *   1. Insert into "customers" table and get back the new customer's ID
     *   2. Insert into "addresses" table using that ID to link the records
     * The address must be inserted second because it needs the customer_id
     * from step 1 — this relationship between tables is called a "foreign key."
     * @param user      the User object containing customer details
     * @param address   the Address object containing address details
     * @return          the newly generated customer_id from the database
     */
    @SuppressWarnings("resource")
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
        var record = dsl.insertInto(DSL.table("customers"))
                .set(DSL.field("email"), user.getEmail())
                .set(DSL.field("first_name"), user.getFirstName())
                .set(DSL.field("last_name"), user.getLastName())
                .set(DSL.field("phone_number"), user.getPhoneNumber())
                .set(DSL.field("password_hash"), user.getPassword())
                .returning(DSL.field("customer_id"))
                .fetchOne();

        if (record == null) {
            throw new RuntimeException("Failed to insert customer — no ID returned.");
        }

        Long newCustomerId = record.get(DSL.field("customer_id", Integer.class)).longValue();

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