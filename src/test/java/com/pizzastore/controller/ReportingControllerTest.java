package com.pizzastore.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({ReportingController.class, GlobalExceptionHandler.class})
public class ReportingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // --- GET /api/reports ---

    @Test
    public void getReports_returnsOk() throws Exception {
        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isOk());
    }

    @Test
    public void getReports_returnsFiveEntries() throws Exception {
        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    public void getReports_eachEntryHasNameAndValueFields() throws Exception {
        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").isNotEmpty())
                .andExpect(jsonPath("$[0].value").exists())
                .andExpect(jsonPath("$[1].name").isNotEmpty())
                .andExpect(jsonPath("$[1].value").exists());
    }

    @Test
    public void getReports_containsExpectedReportNames() throws Exception {
        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'Total Orders')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'Revenue This Month')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'Average Order Value')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'Top Selling Pizza')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'Active Customers')]").exists());
    }

    @Test
    public void getReports_containsExpectedValues() throws Exception {
        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'Total Orders')].value").value(1248))
                .andExpect(jsonPath("$[?(@.name == 'Top Selling Pizza')].value").value("Margherita"))
                .andExpect(jsonPath("$[?(@.name == 'Active Customers')].value").value(342));
    }
}