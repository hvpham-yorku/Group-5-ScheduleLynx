package ca.yorku.eecs2311.schedulelynx.web.controller;

import static ca.yorku.eecs2311.schedulelynx.web.controller.AuthController.SESSION_USER_ID;
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

  private static final long USER_ID = 1L;

  @Test
  void delete_returnsNoContent_thenGetIsNotFound() throws Exception {
    long id = 1L;

    // Create one task
    mockMvc
        .perform(post("/api/tasks")
                     .sessionAttr(SESSION_USER_ID, USER_ID)
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
    mockMvc
        .perform(
            delete("/api/tasks/{id}", id).sessionAttr(SESSION_USER_ID, USER_ID))
        .andExpect(status().isNoContent());

    // Verify
    mockMvc
        .perform(
            get("/api/tasks/{id}", id).sessionAttr(SESSION_USER_ID, USER_ID))
        .andExpect(status().isNotFound());
  }

  @Test
  void delete_missingTask_returnsNotFound() throws Exception {
    mockMvc
        .perform(delete("/api/tasks/{id}", 999)
                     .sessionAttr(SESSION_USER_ID, USER_ID))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("NOT_FOUND"));
  }
}
