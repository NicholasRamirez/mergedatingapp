package com.merge.mergedatingapp.securityTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Qualifier;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    @Qualifier("filterChain")
    private SecurityFilterChain securityFilterChain;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void securityFilterChainBeanExists() {
        assertNotNull(securityFilterChain, "SecurityFilterChain bean should be created");
    }

    @Test
    void anyRequestIsPermittedWithoutAuth() throws Exception {
        mockMvc.perform(get("/test-open"))
                .andExpect(status().isOk())
                .andExpect(content().string("open"));
    }

    @TestConfiguration
    static class TestControllerConfig {

        @RestController
        static class TestOpenController {
            @GetMapping("/test-open")
            public String open() {
                return "open";
            }
        }
    }
}
