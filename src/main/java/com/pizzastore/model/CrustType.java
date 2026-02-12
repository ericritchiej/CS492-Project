package com.pizzastore.model;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "crust_types")
public class CrustType {

    @Id
    @Column(name = "crust_id")
    private Long crustId;

    @Column(name = "crust_name")
    private String crustName;

    @Column(name = "price")
    private BigDecimal price;

    public Long getCrustId() {
        return crustId;
    }

    public void setCrustId(Long crustId) {
        this.crustId = crustId;
    }

    public String getCrustName() {
        return crustName;
    }

    public void setCrustName(String crustName) {
        this.crustName = crustName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
