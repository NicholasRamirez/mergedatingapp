package com.merge.mergedatingapp.authTest;

import com.merge.mergedatingapp.auth.AuthController;
import com.merge.mergedatingapp.auth.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.merge.mergedatingapp.auth.dto.LoginRequest;
import com.merge.mergedatingapp.auth.dto.UserResponse;
import com.merge.mergedatingapp.auth.dto.RegisterRequest;
import com.merge.mergedatingapp.auth.dto.TokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void register_callsServiceAndReturns201() throws Exception {
        RegisterRequest req = new RegisterRequest("test@example.com", "Password123!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void login_returnsTokenFromService() throws Exception {
        LoginRequest req = new LoginRequest("test@example.com", "Password123!");
        TokenResponse token = new TokenResponse("dev-123");
        when(authService.login(any(LoginRequest.class))).thenReturn(token);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("dev-123")));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void me_usesAuthorizationHeaderAndReturnsUserInfo() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResponse me = new UserResponse(userId, "me@example.com");

        String header = "Bearer dev-" + userId;
        when(authService.getUserFromToken(header)).thenReturn(me);

        mockMvc.perform(get("/api/auth/user")
                        .header("Authorization", header))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(userId.toString())))
                .andExpect(jsonPath("$.email", is("me@example.com")));

        verify(authService).getUserFromToken(eq(header));
    }
}