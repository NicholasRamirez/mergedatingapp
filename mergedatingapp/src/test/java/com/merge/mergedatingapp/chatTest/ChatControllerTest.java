package com.merge.mergedatingapp.chatTest;

import com.merge.mergedatingapp.chat.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.merge.mergedatingapp.auth.AuthService;
import com.merge.mergedatingapp.auth.dto.UserResponse;
import com.merge.mergedatingapp.chat.dto.MessageRequest;
import com.merge.mergedatingapp.chat.dto.MessageResponse;
import com.merge.mergedatingapp.chat.dto.ThreadSummary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChatControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private static final String AUTH_HEADER =
            "Bearer dev-123e4567-e89b-12d3-a456-426614174000";

    private static final UUID USER_ID =
            UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @TestConfiguration
    static class FakeBeans {

        @Bean
        @Primary
        AuthService fakeAuthService() {
            return new AuthService(null, null, null) {
                @Override
                public UserResponse getUserFromToken(String header) {
                    return new UserResponse(
                            UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
                            "chat@gmail.com"
                    );
                }
            };
        }

        @Bean
        @Primary
        ChatService fakeChatService() {
            return new ChatService(null, null, null, null, null) {
                @Override
                public List<ThreadSummary> listThreads(UUID userId) {
                    return List.of(
                            new ThreadSummary(
                                    UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                                    UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
                                    UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
                                    "Jess"
                            )
                    );
                }

                @Override
                public List<MessageResponse> getMessages(UUID userId, UUID threadId) {
                    return List.of(
                            new MessageResponse(
                                    UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"),
                                    threadId,
                                    userId,
                                    Instant.parse("2024-01-01T00:00:00Z"),
                                    "Hello"
                            )
                    );
                }

                @Override
                public MessageResponse send(UUID userId, UUID threadId, MessageRequest req) {
                    return new MessageResponse(
                            UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"),
                            threadId,
                            userId,
                            Instant.parse("2024-01-01T00:00:01Z"),
                            req.content()
                    );
                }
            };
        }
    }

    @Test
    void threads_returnsThreadList() throws Exception {
        mvc.perform(get("/api/threads")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].partnerName").value("Jess"))
                .andExpect(jsonPath("$[0].threadId")
                        .value("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
    }

    @Test
    void getMessages_returnsMessagesForThread() throws Exception {
        String threadId = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";

        mvc.perform(get("/api/threads/{threadId}/messages", threadId)
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Hello"))
                .andExpect(jsonPath("$[0].threadId").value(threadId));
    }

    @Test
    void send_createsMessageAndReturnsIt() throws Exception {
        String threadId = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
        MessageRequest req = new MessageRequest("Hi there");
        String json = mapper.writeValueAsString(req);

        mvc.perform(post("/api/threads/{threadId}/messages", threadId)
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hi there"))
                .andExpect(jsonPath("$.threadId").value(threadId));
    }
}