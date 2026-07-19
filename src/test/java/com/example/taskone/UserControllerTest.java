package com.example.taskone;

import com.example.taskone.model.Role;
import com.example.taskone.model.User;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WithMockUser(username = "john.doe@example.com", roles = {"USER"})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private org.springframework.web.context.WebApplicationContext context;

    private User validUser;

    @BeforeEach
    void setUp() {
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                .webAppContextSetup(context)
                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
                .build();

        validUser = User.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .age(30)
                .password("password123")
                .role(Role.USER)
                .build();
    }

    @Test
    void createUser_Success() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")))
                .andExpect(jsonPath("$.age", is(30)));
    }

    @Test
    void createUser_InvalidEmail() throws Exception {
        User invalidUser = User.builder()
                .name("John Doe")
                .email("invalid-email")
                .age(30)
                .password("password123")
                .role(Role.USER)
                .build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("Email is invalid")));
    }

    @Test
    void createUser_EmptyName() throws Exception {
        User invalidUser = User.builder()
                .name("   ")
                .email("john.doe@example.com")
                .age(30)
                .password("password123")
                .role(Role.USER)
                .build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Name is required")));
    }

    @Test
    void createUser_InvalidAge() throws Exception {
        User invalidUser = User.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .age(-5)
                .password("password123")
                .role(Role.USER)
                .build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Age must be a positive integer")));
    }

    @Test
    void getAllUsers_Success() throws Exception {
        // Create a user first
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("John Doe")));
    }

    @Test
    void getUserById_Success() throws Exception {
        // Create user and extract JSON response to get ID
        String response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        User createdUser = objectMapper.readValue(response, User.class);
        UUID userId = createdUser.getId();

        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId.toString())))
                .andExpect(jsonPath("$.name", is("John Doe")));
    }

    @Test
    void getUserById_NotFound() throws Exception {
        UUID randomId = UUID.randomUUID();
        mockMvc.perform(get("/api/users/" + randomId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("User with ID " + randomId + " not found")));
    }

    @Test
    void updateUser_Success() throws Exception {
        // Create user first
        String response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        User createdUser = objectMapper.readValue(response, User.class);
        UUID userId = createdUser.getId();

        // Update fields
        User updatedDetails = User.builder()
                .name("John Updated")
                .email("john.updated@example.com")
                .age(35)
                .password("newpassword123")
                .role(Role.USER)
                .build();

        mockMvc.perform(put("/api/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId.toString())))
                .andExpect(jsonPath("$.name", is("John Updated")))
                .andExpect(jsonPath("$.email", is("john.updated@example.com")))
                .andExpect(jsonPath("$.age", is(35)));
    }

    @Test
    void updateUser_NotFound() throws Exception {
        UUID randomId = UUID.randomUUID();
        mockMvc.perform(put("/api/users/" + randomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void deleteUser_Success() throws Exception {
        // Create user
        String response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        User createdUser = objectMapper.readValue(response, User.class);
        UUID userId = createdUser.getId();

        // Delete user
        mockMvc.perform(delete("/api/users/" + userId))
                .andExpect(status().isNoContent());

        // Verify user is gone
        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_NotFound() throws Exception {
        UUID randomId = UUID.randomUUID();
        mockMvc.perform(delete("/api/users/" + randomId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }
}
