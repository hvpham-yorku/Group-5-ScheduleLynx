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
class FixedEventControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void postAndGetFixedEvents_work() throws Exception {
    mockMvc
        .perform(post("/api/fixed-events")
                     .contentType(MediaType.APPLICATION_JSON)
                     .content("""
                    {
                      "title": "EECS 2311 Lecture",
                      "day": "TUESDAY",
                      "start": "10:00:00",
                      "end": "11:30:00"
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("EECS 2311 Lecture"));

    mockMvc.perform(get("/api/fixed-events"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1));
  }

  @Test
  void missingTitle_returnsBadRequest() throws Exception {
    mockMvc
        .perform(post("/api/fixed-events")
                     .contentType(MediaType.APPLICATION_JSON)
                     .content("""
                    {
                      "title": "",
                      "day": "TUESDAY",
                      "start": "10:00:00",
                      "end": "11:30:00"
                    }
                    """))
        .andExpect(status().isBadRequest());
  }
}
