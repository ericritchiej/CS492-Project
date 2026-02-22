package com.pizzastore.controller;

import com.pizzastore.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({UserController.class, GlobalExceptionHandler.class})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    // --- GET /api/user/getUser ---

    @Test
    public void getUserInfo_returnsOk() throws Exception {
        mockMvc.perform(get("/api/user/getUser"))
                .andExpect(status().isOk());
    }

    @Test
    public void getUserInfo_returnsExpectedFields() throws Exception {
        mockMvc.perform(get("/api/user/getUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.phone").value("555-1234"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.city").value("Eau Claire"))
                .andExpect(jsonPath("$.state").value("WI"))
                .andExpect(jsonPath("$.zip").value("54701"));
    }

    // --- POST /api/user/updateDemographics ---

    @Test
    public void updateDemographics_returnsOk() throws Exception {
        mockMvc.perform(post("/api/user/updateDemographics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Jane\",\"lastName\":\"Smith\",\"phone\":\"555-9999\"," +
                                 "\"address1\":\"789 Elm St\",\"address2\":\"\",\"city\":\"Madison\"," +
                                 "\"state\":\"WI\",\"zip\":\"53703\",\"email\":\"jane.smith@example.com\"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void updateDemographics_returnsEchoedBody() throws Exception {
        mockMvc.perform(post("/api/user/updateDemographics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Jane\",\"lastName\":\"Smith\",\"phone\":\"555-9999\"," +
                                 "\"address1\":\"789 Elm St\",\"address2\":\"\",\"city\":\"Madison\"," +
                                 "\"state\":\"WI\",\"zip\":\"53703\",\"email\":\"jane.smith@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.email").value("jane.smith@example.com"))
                .andExpect(jsonPath("$.city").value("Madison"));
    }
}