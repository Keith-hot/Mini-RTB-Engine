package com.interview.rtb.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;

import static com.interview.rtb.event.ClickAttributionStore.ClickAdmission.ALREADY_CLICKED;
import static com.interview.rtb.event.ClickAttributionStore.ClickAdmission.UNKNOWN_IMPRESSION;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final AdEventPublisher publisher;
    private final ClickAttributionStore clickAttributionStore;
    private final Clock clock;

    public EventController(AdEventPublisher publisher, ClickAttributionStore clickAttributionStore, Clock clock) {
        this.publisher = publisher;
        this.clickAttributionStore = clickAttributionStore;
        this.clock = clock;
    }

    @PostMapping("/click")
    public AdEvent click(@Valid @RequestBody ClickRequest request) {
        ClickAttributionStore.ClickAdmission admission = clickAttributionStore.admitClick(
                request.impressionRequestId(),
                request.userId(),
                request.campaignId()
        );
        if (admission == UNKNOWN_IMPRESSION) {
            throw new ResponseStatusException(NOT_FOUND);
        }
        if (admission == ALREADY_CLICKED) {
            throw new ResponseStatusException(CONFLICT);
        }
        AdEvent event = AdEvent.click(request.requestId(), request.userId(), request.campaignId(), clock.instant());
        publisher.publish(event);
        return event;
    }

    public record ClickRequest(
            @NotBlank String requestId,
            @NotBlank String impressionRequestId,
            @NotBlank String userId,
            @NotNull @Positive Long campaignId
    ) {
    }
}
