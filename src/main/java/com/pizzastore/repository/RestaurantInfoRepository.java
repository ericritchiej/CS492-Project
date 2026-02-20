package com.pizzastore.repository;

import com.pizzastore.model.RestaurantInfo;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RestaurantInfoRepository {

    private final DSLContext dsl;
    private static final Logger logger = LoggerFactory.getLogger(RestaurantInfoRepository.class);

    /**
     * Constructor — Spring calls this automatically at startup and injects
     * the DSLContext for us. This is called "Dependency Injection."
     * We store it in a field so all methods in this class can use it.
     */
    public RestaurantInfoRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @SuppressWarnings("resource")
    public List<RestaurantInfo> findRestaurantInfo() {
        logger.info("Finding restaurant information");

        // Build the SELECT query using jOOQ.
        // This is equivalent to the following SQL:
        //   SELECT *
        //   FROM restaurant
        //
        return dsl.select(
                        DSL.field("id"),
                        DSL.field("name"),
                        DSL.field("street_addr_1"),
                        DSL.field("street_addr_2"),
                        DSL.field("city"),
                        DSL.field("state"),
                        DSL.field("zip_code"),
                        DSL.field("phone_number"),
                        DSL.field("description")
                )
                .from(DSL.table("restaurant"))
                .fetchInto(RestaurantInfo.class);
    }

}