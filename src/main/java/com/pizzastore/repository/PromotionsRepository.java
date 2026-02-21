package com.pizzastore.repository;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import java.util.Map;

@Repository
public class PromotionsRepository {

    private final DSLContext dsl;

    public PromotionsRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * Returns every row and every column from the 'promotions' table.
     * Using fetchMaps() keeps this flexible even if the table columns change.
     */
    public List<Map<String, Object>> findAll() {
        return dsl.select()
                .from(DSL.table("promotions"))
                .fetchMaps();
    }

    /**
     * Creates a promotion row. Assumes promotion_id is DB-generated.
     * Columns expected: code, discount_value, promotion_desc, promotion_summary, exp_dt, min_order_amt
     */
    public boolean createPromotion(String code,
                                   BigDecimal discountValue,
                                   String promotionDesc,
                                   String promotionSummary,
                                   Date expDt,
                                   BigDecimal minOrderAmt) {
        int inserted = dsl.insertInto(DSL.table("promotions"))
                .columns(
                        DSL.field("code"),
                        DSL.field("discount_value"),
                        DSL.field("promotion_desc"),
                        DSL.field("promotion_summary"),
                        DSL.field("exp_dt"),
                        DSL.field("min_order_amt")
                )
                .values(code, discountValue, promotionDesc, promotionSummary, expDt, minOrderAmt)
                .execute();

        return inserted == 1;
    }

    /**
     * Updates a promotion row identified by promotion_id.
     * Columns expected: promotion_id, code, discount_value, promotion_desc, promotion_summary, exp_dt, min_order_amt
     *
     * @return true if exactly one row was updated
     */
    public boolean updatePromotion(long promotionId,
                                   String code,
                                   BigDecimal discountValue,
                                   String promotionDesc,
                                   String promotionSummary,
                                   Date expDt,
                                   BigDecimal minOrderAmt) {
        int updated = dsl.update(DSL.table("promotions"))
                .set(DSL.field("code"), code)
                .set(DSL.field("discount_value"), discountValue)
                .set(DSL.field("promotion_desc"), promotionDesc)
                .set(DSL.field("promotion_summary"), promotionSummary)
                .set(DSL.field("exp_dt"), expDt)
                .set(DSL.field("min_order_amt"), minOrderAmt)
                .where(DSL.field("promotion_id").eq(promotionId))
                .execute();

        return updated == 1;
    }

    /**
     * Deletes a promotion row identified by promotion_id.
     *
     * @return true if exactly one row was deleted
     */
    public boolean deletePromotion(long promotionId) {
        int deleted = dsl.deleteFrom(DSL.table("promotions"))
                .where(DSL.field("promotion_id").eq(promotionId))
                .execute();

        return deleted == 1;
    }
}
