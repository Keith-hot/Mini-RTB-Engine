package com.interview.rtb.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final AdEventPublisher publisher;
    private final Clock clock;

    public EventController(AdEventPublisher publisher, Clock clock) {
        this.publisher = publisher;
        this.clock = clock;
    }

    @PostMapping("/click")
    public AdEvent click(@Valid @RequestBody ClickRequest request) {
        AdEvent event = AdEvent.click(request.requestId(), request.userId(), request.campaignId(), clock.instant());
        publisher.publish(event);
        return event;
    }

    public record ClickRequest(
            @NotBlank String requestId,
            @NotBlank String userId,
            @NotNull @Positive Long campaignId
    ) {
    }
}
