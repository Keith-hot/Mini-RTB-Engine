package com.interview.rtb.event;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventControllerClickAttributionTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-05-31T10:00:00Z"), ZoneOffset.UTC);

    @Test
    void publishesClickForKnownImpressionOnlyOnce() {
        InMemoryAdEventPublisher publisher = new InMemoryAdEventPublisher();
        ClickAttributionStore attributionStore = new ClickAttributionStore();
        attributionStore.record(AdEvent.impression("bid-1", "user-1", 101L, clock.instant()));
        EventController controller = new EventController(publisher, attributionStore, clock);

        AdEvent click = controller.click(new EventController.ClickRequest("click-1", "bid-1", "user-1", 101L));

        assertThat(click.type()).isEqualTo(AdEventType.CLICK);
        assertThat(click.requestId()).isEqualTo("click-1");
        assertThat(publisher.events()).containsExactly(click);
    }

    @Test
    void rejectsClickForUnknownImpression() {
        InMemoryAdEventPublisher publisher = new InMemoryAdEventPublisher();
        ClickAttributionStore attributionStore = new ClickAttributionStore();
        EventController controller = new EventController(publisher, attributionStore, clock);

        assertThatThrownBy(() -> controller.click(new EventController.ClickRequest("click-1", "missing-bid", "user-1", 101L)))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(error -> ((ResponseStatusException) error).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(publisher.events()).isEmpty();
    }

    @Test
    void rejectsDuplicateClickForSameImpression() {
        InMemoryAdEventPublisher publisher = new InMemoryAdEventPublisher();
        ClickAttributionStore attributionStore = new ClickAttributionStore();
        attributionStore.record(AdEvent.impression("bid-1", "user-1", 101L, clock.instant()));
        EventController controller = new EventController(publisher, attributionStore, clock);

        controller.click(new EventController.ClickRequest("click-1", "bid-1", "user-1", 101L));

        assertThatThrownBy(() -> controller.click(new EventController.ClickRequest("click-2", "bid-1", "user-1", 101L)))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(error -> ((ResponseStatusException) error).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        assertThat(publisher.events()).hasSize(1);
    }
}
