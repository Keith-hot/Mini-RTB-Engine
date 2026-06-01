package com.interview.rtb.event;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryAdEventPublisherTest {

    @Test
    void keepsOnlyMostRecentEvents() {
        InMemoryAdEventPublisher publisher = new InMemoryAdEventPublisher(3);

        publisher.publish(AdEvent.impression("req-1", "user-1", 1L, Instant.parse("2026-05-31T10:00:00Z")));
        publisher.publish(AdEvent.impression("req-2", "user-1", 1L, Instant.parse("2026-05-31T10:00:01Z")));
        publisher.publish(AdEvent.impression("req-3", "user-1", 1L, Instant.parse("2026-05-31T10:00:02Z")));
        publisher.publish(AdEvent.impression("req-4", "user-1", 1L, Instant.parse("2026-05-31T10:00:03Z")));

        assertThat(publisher.events()).extracting(AdEvent::requestId)
                .containsExactly("req-2", "req-3", "req-4");
    }

    @Test
    void clearRemovesStoredEvents() {
        InMemoryAdEventPublisher publisher = new InMemoryAdEventPublisher();
        publisher.publish(AdEvent.impression("req-1", "user-1", 1L, Instant.parse("2026-05-31T10:00:00Z")));

        publisher.clear();

        assertThat(publisher.events()).isEmpty();
    }
}
