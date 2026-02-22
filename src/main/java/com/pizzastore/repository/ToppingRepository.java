package com.pizzastore.repository;

import com.pizzastore.model.Topping;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ToppingRepository {

    private final DSLContext dsl;

    public ToppingRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    //    This is just like a SQL statement
    public List<Topping> findAll() {
        return dsl.select(
                        DSL.field("topping_id"),
                        DSL.field("topping_name"),
                        DSL.field("extra_cost")
                )
                .from(DSL.table("toppings"))
                .fetchInto(Topping.class);
    }

    public void insertNewTopping(Topping topping) {

        dsl.insertInto(DSL.table("toppings"))
                .set(DSL.field("topping_name"), topping.getToppingName())
                .set(DSL.field("extra_cost"), topping.getExtraCost())
                .execute();
    }

    public void deleteTopping(Long toppingId) {

        dsl.deleteFrom(DSL.table("toppings"))
                .where(DSL.field("topping_id").eq(toppingId))
                .execute();
    }

    public void updateTopping(Topping topping) {

        dsl.update(DSL.table("toppings"))
                .set(DSL.field("topping_name"), topping.getToppingName())
                .set(DSL.field("extra_cost"), topping.getExtraCost())
                .where(DSL.field("topping_id").eq(topping.getToppingId()))
                .execute();
    }
}
