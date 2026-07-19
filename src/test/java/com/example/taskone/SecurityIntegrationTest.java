package com.example.taskone;

import com.example.taskone.dto.LoginRequest;
import com.example.taskone.dto.RegisterRequest;
import com.example.taskone.model.Role;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private org.springframework.web.context.WebApplicationContext context;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                .webAppContextSetup(context)
                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void registerAndLogin_Success() throws Exception {
        // 1. Register a new user
        RegisterRequest registerRequest = RegisterRequest.builder()
                .name("Alice Security")
                .email("alice@example.com")
                .age(25)
                .password("securepassword")
                .role(Role.USER)
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is("alice@example.com")))
                .andExpect(jsonPath("$.role", is("USER")))
                .andExpect(jsonPath("$.password", notNullValue())); // hashed

        // 2. Login to retrieve JWT
        LoginRequest loginRequest = LoginRequest.builder()
                .email("alice@example.com")
                .password("securepassword")
                .build();

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.type", is("Bearer")))
                .andExpect(jsonPath("$.email", is("alice@example.com")))
                .andExpect(jsonPath("$.role", is("USER")))
                .andReturn();

        // Parse token
        String responseContent = loginResult.getResponse().getContentAsString();
        Map<?, ?> responseMap = objectMapper.readValue(responseContent, Map.class);
        String token = (String) responseMap.get("token");

        // 3. Request /api/profile WITHOUT token -> 401 Unauthorized
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isUnauthorized());

        // 4. Request /api/profile WITH token -> 200 OK
        mockMvc.perform(get("/api/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("alice@example.com")))
                .andExpect(jsonPath("$.name", is("Alice Security")));
    }

    @Test
    void registerAdmin_AccessAdminDashboard_Success() throws Exception {
        // 1. Register an ADMIN user
        RegisterRequest registerRequest = RegisterRequest.builder()
                .name("Admin Boss")
                .email("admin@example.com")
                .age(35)
                .password("adminpassword")
                .role(Role.ADMIN)
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // 2. Login to get token
        LoginRequest loginRequest = LoginRequest.builder()
                .email("admin@example.com")
                .password("adminpassword")
                .build();

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String token = (String) objectMapper.readValue(loginResult.getResponse().getContentAsString(), Map.class).get("token");

        // 3. Request /api/admin/dashboard WITH ADMIN token -> 200 OK
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("Welcome Admin")));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void getAdminDashboard_WithUserRole_Forbidden() throws Exception {
        // User with ROLE_USER attempts to access admin dashboard -> 403 Forbidden
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isForbidden());
    }
}
