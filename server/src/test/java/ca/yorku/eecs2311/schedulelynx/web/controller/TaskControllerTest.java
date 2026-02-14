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
class TaskControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void put_updatesTask() throws Exception {
    // 1) Create
    String createdJson =
        mockMvc
            .perform(post("/api/tasks")
                         .contentType(MediaType.APPLICATION_JSON)
                         .content("""
                        {
                          "title": "Old title",
                          "dueDate": "2026-02-13",
                          "estimatedHours": 6,
                          "difficulty": "HIGH"
                        }
                        """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();

    // First task has id 1 in a fresh repo.
    long id = 1L;

    // 2) Update
    mockMvc
        .perform(put("/api/tasks/{id}", id)
                     .contentType(MediaType.APPLICATION_JSON)
                     .content("""
                    {
                      "title": "New title",
                      "dueDate": "2026-02-14",
                      "estimatedHours": 4,
                      "difficulty": "MEDIUM"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("New title"))
        .andExpect(jsonPath("$.dueDate").value("2026-02-14"))
        .andExpect(jsonPath("$.estimatedHours").value(4))
        .andExpect(jsonPath("$.difficulty").value("MEDIUM"));
  }

  @Test
  void delete_returnsNoContent_thenGetIsNotFound() throws Exception {
    long id = 1L;

    // Create one task
    mockMvc
        .perform(post("/api/tasks")
                     .contentType(MediaType.APPLICATION_JSON)
                     .content("""
                    {
                      "title": "To delete",
                      "dueDate": "2026-02-13",
                      "estimatedHours": 2,
                      "difficulty": "LOW"
                    }
                    """))
        .andExpect(status().isCreated());

    // Delete it
    mockMvc.perform(delete("/api/tasks/{id}", id))
        .andExpect(status().isNoContent());

    // Verify
    mockMvc.perform(get("/api/tasks/{id}", id))
        .andExpect(status().isNotFound());
  }

  @Test
  void delete_missingTask_returnsNotFound() throws Exception {
    mockMvc.perform(delete("/api/tasks/{id}", 999))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("NOT_FOUND"));
  }
}
