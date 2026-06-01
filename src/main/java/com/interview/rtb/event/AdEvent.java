package com.interview.rtb.event;

import java.time.Instant;

public record AdEvent(
        AdEventType type,
        String requestId,
        String userId,
        long campaignId,
        Instant occurredAt
) {
    public static AdEvent impression(String requestId, String userId, long campaignId, Instant occurredAt) {
        return new AdEvent(AdEventType.IMPRESSION, requestId, userId, campaignId, occurredAt);
    }

    public static AdEvent click(String requestId, String userId, long campaignId, Instant occurredAt) {
        return new AdEvent(AdEventType.CLICK, requestId, userId, campaignId, occurredAt);
    }
}
