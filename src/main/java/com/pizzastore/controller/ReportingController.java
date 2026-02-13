package com.pizzastore.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ReportingController {

    @GetMapping("/reports")
    public List<Map<String, Object>> getReports() {
        return Arrays.asList(
            report("Total Orders", 1248),
            report("Revenue This Month", 38420),
            report("Average Order Value", 30.79),
            report("Top Selling Pizza", "Margherita"),
            report("Active Customers", 342)
        );
    }

    private Map<String, Object> report(String name, Object value) {
        Map<String, Object> r = new HashMap<>();
        r.put("name", name);
        r.put("value", value);
        return r;
    }
}
