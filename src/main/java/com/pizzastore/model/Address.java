package com.pizzastore.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;

/**
 * This is a "Model" class — it represents a row in the "addresses" database table.
 * Think of it as a blueprint that maps Java code to database data.
 *
 * Lombok Annotations (the @Data, @NoArgsConstructor, @AllArgsConstructor lines)
 * are shortcuts that automatically generate repetitive code for us:
 *
 *   @Data            — generates getters, setters, equals(), hashCode(), and toString()
 *                      so we don't have to write methods like getCity() or setCity() manually
 *   @NoArgsConstructor — generates an empty constructor: new Address()
 *                        Spring and jOOQ need this to create Address objects when
 *                        reading data back from the database
 *   @AllArgsConstructor — generates a constructor with every field as a parameter:
 *                         new Address(id, address1, address2, city, state, zip)
 *                         useful when you want to create a fully populated object in one line
 */

/**
 * @Entity tells Spring/JPA that this class maps to a database table.
 * Without this, Spring would treat it as a plain Java class with no database connection.
 *
 * @Table(name = "addresses") tells Spring which table in the database this maps to.
 * If your Java class name matched the table name exactly, you could skip this,
 * but it's good practice to be explicit.
 */
@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    /**
     * @Id marks this field as the primary key — the unique identifier for each row.
     * Every database table should have a primary key so rows can be uniquely identified.
     *
     * @GeneratedValue(strategy = GenerationType.IDENTITY) means the database will
     * automatically generate this value when a new row is inserted.
     * You never set this yourself — the database handles it.
     * In PostgreSQL this is typically handled by a SERIAL or auto-increment column.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * @Column(name = "street_addr_1") maps this Java field to the "street_addr_1"
     * column in the database table. We need this because the Java field name (address1)
     * is different from the database column name (street_addr_1).
     *
     * If the names matched exactly, you could skip the @Column annotation,
     * but being explicit makes the mapping clear and easy to understand.
     */
    @Column(name = "street_addr_1")
    private String address1;

    /**
     * The second address line is typically optional — used for apartment numbers,
     * suite numbers, floor numbers, etc. (e.g. "Apt 4B" or "Suite 200").
     * Having a separate field for this keeps address data clean and structured.
     */
    @Column(name = "street_addr_2")
    private String address2;

    /**
     * City, state, and zip are stored as separate fields rather than one big
     * "address" string. This makes it much easier to search, sort, or filter
     * by location later — for example, finding all customers in a specific city.
     */
    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    /**
     * Zip code is stored as a String rather than an Integer for two reasons:
     * 1. Some zip codes start with 0 (e.g. "06001" in Connecticut) — integers
     *    would drop the leading zero, turning it into 6001 which is wrong.
     * 2. We never do math on zip codes, so there's no benefit to storing as a number.
     */
    @Column(name = "zip_code")
    private String zip;
}