package com.pizzastore.controller;

import com.pizzastore.model.Address;
import com.pizzastore.model.User;
import com.pizzastore.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = {UserController.class, GlobalExceptionHandler.class},
            excludeAutoConfiguration = SecurityAutoConfiguration.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    // --- GET /api/user ---

    @Test
    public void getUser_returnsUnauthorizedWithNoSession() throws Exception {
        mockMvc.perform(get("/api/user"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getUser_returnsProfile() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("jane@gmail.com");
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setPhoneNumber("555-1234");

        Address address = new Address();
        address.setAddress1("123 Main St");
        address.setAddress2("");
        address.setCity("Eau Claire");
        address.setState("WI");
        address.setZip("54701");

        when(userRepository.findCustomerById(1L)).thenReturn(user);
        when(userRepository.findAddressByCustomerId(1L)).thenReturn(address);

        mockMvc.perform(get("/api/user").sessionAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jane@gmail.com"))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.phone").value("555-1234"))
                .andExpect(jsonPath("$.address.address1").value("123 Main St"))
                .andExpect(jsonPath("$.address.city").value("Eau Claire"))
                .andExpect(jsonPath("$.address.state").value("WI"))
                .andExpect(jsonPath("$.address.zip").value("54701"));
    }

    // --- PUT /api/user ---

    @Test
    public void updateUser_returnsUnauthorizedWithNoSession() throws Exception {
        mockMvc.perform(put("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Jane\",\"lastName\":\"Smith\",\"phone\":\"555-9999\"," +
                                 "\"address1\":\"789 Elm St\",\"address2\":\"\",\"city\":\"Madison\"," +
                                 "\"state\":\"WI\",\"zip\":\"53703\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void updateUser_returnsSuccess() throws Exception {
        mockMvc.perform(put("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Jane\",\"lastName\":\"Smith\",\"phone\":\"555-9999\"," +
                                 "\"address1\":\"789 Elm St\",\"address2\":\"\",\"city\":\"Madison\"," +
                                 "\"state\":\"WI\",\"zip\":\"53703\"}")
                        .sessionAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Profile updated successfully"));
    }
}
