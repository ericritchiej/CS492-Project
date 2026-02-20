package com.pizzastore.repository;

import com.pizzastore.model.RestaurantHours;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RestaurantHoursRepository {

    private final DSLContext dsl;
    private static final Logger logger = LoggerFactory.getLogger(RestaurantHoursRepository.class);

    /**
     * Constructor — Spring calls this automatically at startup and injects
     * the DSLContext for us. This is called "Dependency Injection."
     * We store it in a field so all methods in this class can use it.
     */
    public RestaurantHoursRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @SuppressWarnings("resource")
    public List<RestaurantHours> findRestaurantHours() {
        logger.info("Finding restaurant hours");

        // Build the SELECT query using jOOQ.
        // This is equivalent to the following SQL:
        //   SELECT *
        //   FROM restaurant_hours
        //
        return dsl.select(
                        DSL.field("id"),
                        DSL.field("restaurant_id"),
                        DSL.field("display_text"),
                        DSL.field("sort_order")
                )
                .from(DSL.table("restaurant_hours"))
                .orderBy(DSL.field("sort_order").asc())
                .fetchInto(RestaurantHours.class);
    }

}