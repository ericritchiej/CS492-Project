package com.pizzastore.repository;

import com.pizzastore.model.Address;
import com.pizzastore.model.User;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepository {

    private final DSLContext dsl;
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

    public UserRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    // =========================
    // Auth / Registration
    // =========================

    @SuppressWarnings("resource")
    public List<User> findByUsername(String username) {
        logger.info("Finding users by username {}", username);

        return dsl.select(
                        // Alias to Java field names for jOOQ -> POJO mapping
                        DSL.field("customer_id").as("id"),
                        DSL.field("email").as("email"),
                        DSL.field("first_name").as("firstName"),
                        DSL.field("last_name").as("lastName"),
                        DSL.field("phone_number").as("phoneNumber"),
                        DSL.field("password_hash").as("password")
                )
                .from(DSL.table("customers"))
                .where(DSL.field("email").eq(username))
                .fetchInto(User.class);
    }

    @SuppressWarnings("resource")
    public Long createUser(User user, Address address) {
        logger.info("Creating new user with email {}", user.getEmail());

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

        dsl.insertInto(DSL.table("addresses"))
                .set(DSL.field("customer_id"), newCustomerId)
                .set(DSL.field("street_addr_1"), address.getAddress1())
                .set(DSL.field("street_addr_2"), address.getAddress2())
                .set(DSL.field("city"), address.getCity())
                .set(DSL.field("state"), address.getState())
                .set(DSL.field("zip_code"), address.getZip())
                .execute();

        return newCustomerId;
    }

    // =========================
    // SCRUM-17: Profile methods
    // =========================

    /**
     * Fetch a customer by ID (for profile page).
     * NOTE: Intentionally does NOT select password_hash.
     */
    @SuppressWarnings("resource")
    public User findCustomerById(long id) {
        logger.info("Finding customer by id {}", id);

        return dsl.select(
                        DSL.field("customer_id").as("id"),
                        DSL.field("email").as("email"),
                        DSL.field("first_name").as("firstName"),
                        DSL.field("last_name").as("lastName"),
                        DSL.field("phone_number").as("phoneNumber")
                )
                .from(DSL.table("customers"))
                .where(DSL.field("customer_id").eq(id))
                .fetchOneInto(User.class);
    }

    /**
     * Fetch a customer's address by customer_id.
     */
    @SuppressWarnings("resource")
    public Address findAddressByCustomerId(long customerId) {
        logger.info("Finding address for customerId {}", customerId);

        return dsl.select(
                        DSL.field("street_addr_1").as("address1"),
                        DSL.field("street_addr_2").as("address2"),
                        DSL.field("city").as("city"),
                        DSL.field("state").as("state"),
                        DSL.field("zip_code").as("zip")
                )
                .from(DSL.table("addresses"))
                .where(DSL.field("customer_id").eq(customerId))
                .fetchOneInto(Address.class);
    }

    /**
     * Update customer's basic profile fields.
     */
    @SuppressWarnings("resource")
    public void updateCustomerProfile(long id, String first, String last, String phone) {
        logger.info("Updating customer profile for id {}", id);

        dsl.update(DSL.table("customers"))
                .set(DSL.field("first_name"), first)
                .set(DSL.field("last_name"), last)
                .set(DSL.field("phone_number"), phone)
                .where(DSL.field("customer_id").eq(id))
                .execute();
    }

    /**
     * Update customer's address (update if exists, insert if missing).
     */
    @SuppressWarnings("resource")
    public void updateCustomerAddress(long customerId,
                                      String addr1,
                                      String addr2,
                                      String city,
                                      String state,
                                      String zip) {
        logger.info("Updating customer address for customerId {}", customerId);

        int rows = dsl.update(DSL.table("addresses"))
                .set(DSL.field("street_addr_1"), addr1)
                .set(DSL.field("street_addr_2"), addr2)
                .set(DSL.field("city"), city)
                .set(DSL.field("state"), state)
                .set(DSL.field("zip_code"), zip)
                .where(DSL.field("customer_id").eq(customerId))
                .execute();

        // If there was no existing address row, insert one
        if (rows == 0) {
            dsl.insertInto(DSL.table("addresses"))
                    .set(DSL.field("customer_id"), customerId)
                    .set(DSL.field("street_addr_1"), addr1)
                    .set(DSL.field("street_addr_2"), addr2)
                    .set(DSL.field("city"), city)
                    .set(DSL.field("state"), state)
                    .set(DSL.field("zip_code"), zip)
                    .execute();
        }
    }
}
