package com.pizzastore.repository;

import com.pizzastore.model.Address;
import com.pizzastore.model.Employee;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * A "Repository" is responsible for all communication with the database.
 * Think of it as the only class that is allowed to speak "database language."
 *
 * This separation is intentional and is called the "Repository Pattern":
 *   - Controllers (AuthController) handle HTTP requests and responses
 *   - Repositories (EmployeeRepository) handle database reads and writes
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
public class EmployeeRepository {

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
    private static final Logger logger = LoggerFactory.getLogger(EmployeeRepository.class);

    /**
     * Constructor — Spring calls this automatically at startup and injects
     * the DSLContext for us. This is called "Dependency Injection."
     * We store it in a field so all methods in this class can use it.
     */
    public EmployeeRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * Looks up an employee in the database by their email address.
     * We use email as the "username" for login purposes.
     *
     * Returns a List<Employee> rather than a single User because the database
     * query could theoretically return multiple rows. In practice we expect
     * zero (user not found) or one (user found), but using a List lets us
     * handle both cases safely without exceptions.
     *
     * @param username  the email address to search for
     * @return          a list of matching users (usually empty or one item)
     */
    public List<Employee> findByUsername(String username) {
        logger.info("Finding employees by username {}", username);

        return dsl.select(
                        DSL.field("employee_id"),
                        DSL.field("first_name"),
                        DSL.field("last_name"),
                        DSL.field("email"),
                        DSL.field("password_hash"),
                        DSL.field("role")
                )
                .from(DSL.table("employees"))
                .where(DSL.field("email").eq(username))
                .fetchInto(Employee.class);
    }
}