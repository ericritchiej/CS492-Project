package com.pizzastore.repository;

import com.pizzastore.model.CrustType;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CrustTypeRepository {

    private final DSLContext dsl;

    public CrustTypeRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    //    This is just like a SQL statement
    public List<CrustType> findAll() {
        return dsl.select(
                        DSL.field("crust_id"),
                        DSL.field("crust_name"),
                        DSL.field("price")
                )
                .from(DSL.table("crust_types"))
                .fetchInto(CrustType.class);
    }

    public void insertNewCrustType(CrustType crust) {

        dsl.insertInto(DSL.table("crust_types"))
                .set(DSL.field("crust_name"), crust.getCrustName())
                .set(DSL.field("price"), crust.getPrice())
                .returning(
                        DSL.field("crust_id"),
                        DSL.field("crust_name"),
                        DSL.field("price")
                )
                .execute();
    }

    public void deleteCrustType(Long crustId) {

        dsl.deleteFrom(DSL.table("crust_types"))
                .where(DSL.field("crust_id").eq(crustId))
                .execute();
    }

    public void updateCrustType(CrustType crust) {

        dsl.update(DSL.table("crust_types"))
                .set(DSL.field("crust_name"), crust.getCrustName())
                .set(DSL.field("price"), crust.getPrice())
                .where(DSL.field("crust_id").eq(crust.getCrustId()))
                .returning(
                        DSL.field("crust_id"),
                        DSL.field("crust_name"),
                        DSL.field("price")
                )
                .execute();
    }
}