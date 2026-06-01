package com.interview.rtb.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ClickAttributionStore {

    private final Map<String, ImpressionClickState> impressions = new HashMap<>();

    @EventListener
    public synchronized void record(AdEvent event) {
        if (event.type() == AdEventType.IMPRESSION) {
            impressions.put(event.requestId(), new ImpressionClickState(event.userId(), event.campaignId(), false));
        }
    }

    public synchronized ClickAdmission admitClick(String impressionRequestId, String userId, long campaignId) {
        ImpressionClickState state = impressions.get(impressionRequestId);
        if (state == null || !state.matches(userId, campaignId)) {
            return ClickAdmission.UNKNOWN_IMPRESSION;
        }
        if (state.clicked()) {
            return ClickAdmission.ALREADY_CLICKED;
        }
        impressions.put(impressionRequestId, state.markClicked());
        return ClickAdmission.ADMITTED;
    }

    public synchronized void reset() {
        impressions.clear();
    }

    public enum ClickAdmission {
        ADMITTED,
        UNKNOWN_IMPRESSION,
        ALREADY_CLICKED
    }

    private record ImpressionClickState(String userId, long campaignId, boolean clicked) {
        private boolean matches(String candidateUserId, long candidateCampaignId) {
            return userId.equals(candidateUserId) && campaignId == candidateCampaignId;
        }

        private ImpressionClickState markClicked() {
            return new ImpressionClickState(userId, campaignId, true);
        }
    }
}
