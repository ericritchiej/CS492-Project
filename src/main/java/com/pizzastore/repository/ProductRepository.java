package com.pizzastore.repository;

import com.pizzastore.model.Product;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductRepository {

    private final DSLContext dsl;

    public ProductRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<Product> findAll() {
        return dsl.select(
                        DSL.field("product_id"),
                        DSL.field("category_id"),
                        DSL.field("product_name"),
                        DSL.field("base_price"),
                        DSL.field("is_customizable")
                )
                .from(DSL.table("products"))
                .fetchInto(Product.class);
    }

    public void insertNewProduct(Product product) {

        dsl.insertInto(DSL.table("products"))
                .set(DSL.field("category_id"), product.getCategoryId())
                .set(DSL.field("product_name"), product.getProductName())
                .set(DSL.field("base_price"), product.getBasePrice())
                .set(DSL.field("is_customizable"), product.isCustomizable())
                .execute();
    }

    public void deleteProduct(Long productId) {

        dsl.deleteFrom(DSL.table("products"))
                .where(DSL.field("product_id").eq(productId))
                .execute();
    }

    public void updateProduct(Product product) {

        dsl.update(DSL.table("products"))
                .set(DSL.field("category_id"), product.getCategoryId())
                .set(DSL.field("product_name"), product.getProductName())
                .set(DSL.field("base_price"), product.getBasePrice())
                .set(DSL.field("is_customizable"), product.isCustomizable())
                .where(DSL.field("product_id").eq(product.getProductId()))
                .execute();
    }

}
