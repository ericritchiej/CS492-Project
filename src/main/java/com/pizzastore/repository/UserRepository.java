package com.pizzastore.repository;

import com.pizzastore.model.User;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.Select;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Repository
public class UserRepository {

    private final DSLContext dsl;

    // Define the logger for this specific class
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

    public UserRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    //    This is just like a SQL statement
    // Change return type to Optional<User> to handle "Not Found" safely
    public List<User> findByUsername(String username) {
        logger.info("Finding users by username {}", username);

        // 1. Define the query but DON'T fetch yet
        Select<?> query = dsl.select(
                        DSL.field("email", String.class),
                        DSL.field("first_name", String.class).as("first_name"),   // Match @Column(name="first_name")
                        DSL.field("last_name", String.class).as("last_name"),     // Match @Column(name="last_name")
                        DSL.field("phone_number", String.class).as("phone_number"), // Match @Column(name="phone_number")
                        DSL.field("password_hash", String.class).as("password_hash")
                )
                .from(DSL.table("customers"))
                .where(DSL.field("email").eq(username));

        // 2. Print the SQL (with ? placeholders)
        logger.info("SQL: " + query.getSQL());

        // 3. Print the SQL with the actual values filled in (Inline)
        // Note: This is for debugging only!
        logger.info("Full SQL: " + dsl.renderInlined(query));

        // 4. Finally, execute it

        // Mapping to your list
        return query.fetchInto(User.class);
}
}
