package com.pizzastore.repository;

import com.pizzastore.model.PizzaSize;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PizzaSizeRepository {

    private final DSLContext dsl;

    public PizzaSizeRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<PizzaSize> findAll() {
        return dsl.select(
                        DSL.field("size_id"),
                        DSL.field("size_name"),
                        DSL.field("price")
                )
                .from(DSL.table("pizza_sizes"))
                .fetchInto(PizzaSize.class);
    }

    public void insertNewPizzaSize(PizzaSize pizzaSize) {

        dsl.insertInto(DSL.table("pizza_sizes"))
                .set(DSL.field("size_name"), pizzaSize.getSizeName())
                .set(DSL.field("price"), pizzaSize.getPrice())
                .execute();
    }

    public void deletePizzaSize(Long pizzaSizeId) {

        dsl.deleteFrom(DSL.table("pizza_sizes"))
                .where(DSL.field("size_id").eq(pizzaSizeId))
                .execute();
    }

    public void updatePizzaSize(PizzaSize pizzaSize) {

        dsl.update(DSL.table("pizza_sizes"))
                .set(DSL.field("size_name"), pizzaSize.getSizeName())
                .set(DSL.field("price"), pizzaSize.getPrice())
                .where(DSL.field("size_id").eq(pizzaSize.getSizeId()))
                .execute();
    }

}