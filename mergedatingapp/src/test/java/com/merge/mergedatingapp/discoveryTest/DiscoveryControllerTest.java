package com.merge.mergedatingapp.discoveryTest;

import com.merge.mergedatingapp.discovery.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.merge.mergedatingapp.auth.AuthService;
import com.merge.mergedatingapp.auth.dto.UserResponse;
import com.merge.mergedatingapp.discovery.dto.CandidateCard;
import com.merge.mergedatingapp.discovery.dto.LikeResponse;
import com.merge.mergedatingapp.profiles.Enums.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DiscoveryController.class)
@AutoConfigureMockMvc(addFilters = false)
class DiscoveryControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;

    private static final String AUTH_HEADER =
            "Bearer dev-123e4567-e89b-12d3-a456-426614174000";

    private UUID userId() {
        return UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    }

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
                            "test@example.com"
                    );
                }
            };
        }

        @Bean
        @Primary
        DiscoveryService fakeDiscoveryService() {
            return new DiscoveryService(null,null,null,null,null,null,null) {

                @Override
                public CandidateCard next(UUID uid) {
                    return new CandidateCard(
                            UUID.randomUUID(),
                            "Jess",
                            "Los Angeles",
                            List.of("p1.jpg"),
                            List.of(new CandidateCard.PromptQA("Q1","A1")),
                            GenderType.FEMALE,
                            PronounsType.SHE_HER,
                            RelationshipIntentType.LONG_TERM,
                            160
                    );
                }

                @Override
                public void pass(UUID viewer, UUID candidate) {
                    // no-op for tests
                }

                @Override
                public LikeResponse like(UUID liker, UUID liked) {
                    return LikeResponse.liked();
                }
            };
        }
    }


    // Test /discovery/next
    @Test
    void next_returnsCandidateCard() throws Exception {
        mvc.perform(get("/api/discovery/next")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jess"))
                .andExpect(jsonPath("$.city").value("Los Angeles"))
                .andExpect(jsonPath("$.genderType").value("FEMALE"));
    }

    // Test /discovery/pass
    @Test
    void pass_works() throws Exception {
        UUID candidate = UUID.randomUUID();
        String json = """
                {"candidateUserId": "%s"}
                """.formatted(candidate);

        mvc.perform(post("/api/discovery/pass")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    // Test /matching/like
    @Test
    void like_returnsResponse() throws Exception {
        UUID liked = UUID.randomUUID();
        String json = """
                {"likedUserId": "%s"}
                """.formatted(liked);

        mvc.perform(post("/api/matching/like")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LIKED"))
                .andExpect(jsonPath("$.matchId").doesNotExist());
    }
}
