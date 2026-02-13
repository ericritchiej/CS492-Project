package com.pizzastore.repository;

import com.pizzastore.model.CrustType;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class CrustTypeRepository {

    private final DSLContext dsl;

    public CrustTypeRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<CrustType> findAll() {
        return dsl.select(
                        DSL.field("crust_id", Long.class),
                        DSL.field("crust_name", String.class),
                        DSL.field("price", BigDecimal.class))
                .from(DSL.table("crust_types"))
                .fetchInto(CrustType.class);
    }
}
