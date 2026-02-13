package com.pizzastore.model;

import java.math.BigDecimal;

public class CrustType {

    private Long crustId;
    private String crustName;
    private BigDecimal price;

    public CrustType() {
    }

    public CrustType(Long crustId, String crustName, BigDecimal price) {
        this.crustId = crustId;
        this.crustName = crustName;
        this.price = price;
    }

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
