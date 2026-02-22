package com.pizzastore.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({ProfileController.class, GlobalExceptionHandler.class})
public class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // --- GET /api/profile ---

    @Test
    public void getProfile_returnsOk() throws Exception {
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isOk());
    }

    @Test
    public void getProfile_returnsExpectedFields() throws Exception {
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.phone").value("(555) 987-6543"))
                .andExpect(jsonPath("$.address").value("456 Oak Avenue, Springfield, IL 62702"));
    }
}
