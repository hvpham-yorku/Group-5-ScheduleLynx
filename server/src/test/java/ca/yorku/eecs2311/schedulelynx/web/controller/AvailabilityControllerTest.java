package ca.yorku.eecs2311.schedulelynx.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AvailabilityControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void postAndGetAvailability_work() throws Exception {
    mockMvc
        .perform(post("/api/availability")
                     .contentType(MediaType.APPLICATION_JSON)
                     .content("""
                    {
                      "day": "MONDAY",
                      "start": "18:00:00",
                      "end": "21:00:00"
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.day").value("MONDAY"));

    mockMvc.perform(get("/api/availability"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1));
  }

  @Test
  void invalidTimeRange_returnsBadRequest() throws Exception {
    mockMvc
        .perform(post("/api/availability")
                     .contentType(MediaType.APPLICATION_JSON)
                     .content("""
                    {
                      "day": "MONDAY",
                      "start": "21:00:00",
                      "end": "18:00:00"
                    }
                    """))
        .andExpect(status().isBadRequest());
  }
}
