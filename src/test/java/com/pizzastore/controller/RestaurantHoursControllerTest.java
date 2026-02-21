package com.pizzastore.controller;

import com.pizzastore.model.RestaurantHours;
import com.pizzastore.repository.RestaurantHoursRepository;
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

@WebMvcTest({RestaurantHoursController.class, GlobalExceptionHandler.class})
public class RestaurantHoursControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestaurantHoursRepository restaurantHoursRepository;

    @Test
    public void getRestaurantHours_returnsOk() throws Exception {
        RestaurantHours monday = new RestaurantHours(1L, 1L, "Monday: 11am - 10pm", 1);
        RestaurantHours tuesday = new RestaurantHours(2L, 1L, "Tuesday: 11am - 10pm", 2);
        when(restaurantHoursRepository.findRestaurantHours()).thenReturn(List.of(monday, tuesday));

        mockMvc.perform(get("/api/restaurant-hours"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].restaurantId").value(1))
                .andExpect(jsonPath("$[0].displayText").value("Monday: 11am - 10pm"))
                .andExpect(jsonPath("$[0].sortOrder").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].displayText").value("Tuesday: 11am - 10pm"));
    }

    @Test
    public void getRestaurantHours_returnsEmptyList() throws Exception {
        // The controller always returns 200 OK; an empty list produces an empty JSON array
        when(restaurantHoursRepository.findRestaurantHours()).thenReturn(List.of());

        mockMvc.perform(get("/api/restaurant-hours"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void getRestaurantHours_repositoryThrows() throws Exception {
        doThrow(new RuntimeException("Database unavailable"))
                .when(restaurantHoursRepository).findRestaurantHours();

        mockMvc.perform(get("/api/restaurant-hours"))
                .andExpect(status().isInternalServerError());
    }
}
