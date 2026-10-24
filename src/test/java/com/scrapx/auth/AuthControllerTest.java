package com.scrapx.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scrapx.auth.dto.LoginRequest;
import com.scrapx.auth.dto.RegisterRequest;
import com.scrapx.common.ApiResponse;
import com.scrapx.user.UserRepository;
import com.scrapx.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AuthControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.scrapx.listing.ListingRepository listingRepository;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        listingRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testRegisterBuyerSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Alice Buyer",
                "alice@example.com",
                "password123",
                "9876543210",
                UserRole.Buyer
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.email").value("alice@example.com"))
                .andExpect(jsonPath("$.data.role").value("Buyer"));
    }

    @Test
    void testRegisterSellerSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Bob Seller",
                "bob@example.com",
                "password123",
                "9876543211",
                UserRole.Seller
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.email").value("bob@example.com"))
                .andExpect(jsonPath("$.data.role").value("Seller"));
    }

    @Test
    void testRegisterDuplicateEmailReturnsBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Charlie",
                "charlie@example.com",
                "password123",
                "9876543212",
                UserRole.Buyer
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testLoginSuccess() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "David",
                "david@example.com",
                "password123",
                "9876543213",
                UserRole.Buyer
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest("david@example.com", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.email").value("david@example.com"));
    }

    @Test
    void testLoginWithBadCredentialsReturnsUnauthorized() throws Exception {
        LoginRequest loginRequest = new LoginRequest("nonexistent@example.com", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testGetMeWithValidJwt() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "Emma User",
                "emma@example.com",
                "password123",
                "9876543214",
                UserRole.Seller
        );

        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(regResult.getResponse().getContentAsString());
        String token = rootNode.path("data").path("token").asText();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("emma@example.com"))
                .andExpect(jsonPath("$.data.name").value("Emma User"))
                .andExpect(jsonPath("$.data.role").value("Seller"));
    }

    @Test
    void testGetMeWithoutTokenReturnsForbiddenOrUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}
