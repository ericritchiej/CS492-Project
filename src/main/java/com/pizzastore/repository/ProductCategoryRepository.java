package com.pizzastore.repository;

import com.pizzastore.model.ProductCategory;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductCategoryRepository {

    private final DSLContext dsl;

    public ProductCategoryRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<ProductCategory> findAll() {
        return dsl.select(
                        DSL.field("category_id"),
                        DSL.field("category_name")
                )
                .from(DSL.table("product_categories"))
                .fetchInto(ProductCategory.class);
    }

    public void insertNewProductCategory(ProductCategory productCategory) {

        dsl.insertInto(DSL.table("product_categories"))
                .set(DSL.field("category_name"), productCategory.getCategoryName())
                .execute();
    }

    public void deleteProductCategory(Long productCategoryId) {

        dsl.deleteFrom(DSL.table("product_categories"))
                .where(DSL.field("category_id").eq(productCategoryId))
                .execute();
    }

    public void updateProductCategory(ProductCategory productCategory) {

        dsl.update(DSL.table("product_categories"))
                .set(DSL.field("category_name"), productCategory.getCategoryName())
                .where(DSL.field("category_id").eq(productCategory.getCategoryId()))
                .execute();
    }

}