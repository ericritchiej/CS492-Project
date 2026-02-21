package com.pizzastore.controller;

import com.pizzastore.model.RestaurantInfo;
import com.pizzastore.repository.RestaurantInfoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({RestaurantInfoController.class, GlobalExceptionHandler.class})
public class RestaurantInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestaurantInfoRepository restaurantInfoRepository;

    @Test
    public void getRestaurantInfo_returnsOk() throws Exception {
        RestaurantInfo info = new RestaurantInfo(1L, "Mario's Pizza", "123 Main St",
                null, "Springfield", "IL", "62701", "5551234567", "Best pizza in town");
        when(restaurantInfoRepository.findRestaurantInfo()).thenReturn(List.of(info));

        mockMvc.perform(get("/api/restaurant-info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mario's Pizza"))
                .andExpect(jsonPath("$.streetAddr1").value("123 Main St"))
                .andExpect(jsonPath("$.city").value("Springfield"))
                .andExpect(jsonPath("$.state").value("IL"))
                .andExpect(jsonPath("$.zipCode").value("62701"))
                .andExpect(jsonPath("$.phoneNumber").value("5551234567"))
                .andExpect(jsonPath("$.description").value("Best pizza in town"));
    }

    @Test
    public void getRestaurantInfo_returnsNotFound() throws Exception {
        when(restaurantInfoRepository.findRestaurantInfo()).thenReturn(List.of());

        mockMvc.perform(get("/api/restaurant-info"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getRestaurantInfo_repositoryThrows() throws Exception {
        doThrow(new RuntimeException("Database unavailable"))
                .when(restaurantInfoRepository).findRestaurantInfo();

        mockMvc.perform(get("/api/restaurant-info"))
                .andExpect(status().isInternalServerError());
    }
}
