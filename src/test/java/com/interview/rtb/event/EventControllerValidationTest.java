package com.interview.rtb.event;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
class EventControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdEventPublisher publisher;

    @MockBean
    private ClickAttributionStore clickAttributionStore;

    @Test
    void rejectsEmptyClickRequest() throws Exception {
        mockMvc.perform(post("/api/events/click")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @TestConfiguration
    static class ClockConfig {
        @Bean
        Clock clock() {
            return Clock.fixed(Instant.parse("2026-05-31T10:00:00Z"), ZoneOffset.UTC);
        }
    }
}
