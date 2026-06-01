package com.interview.rtb.bidding;

import java.math.BigDecimal;
import java.time.Instant;

public record BidResponse(
        boolean matched,
        Long campaignId,
        Long creativeId,
        BigDecimal bidPrice,
        String creativeUrl,
        String landingUrl,
        String reason,
        Instant decidedAt
) {

    public static BidResponse noBid(String reason, Instant decidedAt) {
        return new BidResponse(false, null, null, BigDecimal.ZERO, null, null, reason, decidedAt);
    }

    public static BidResponse win(long campaignId, long creativeId, BigDecimal bidPrice,
                                  String creativeUrl, String landingUrl, Instant decidedAt) {
        return new BidResponse(true, campaignId, creativeId, bidPrice, creativeUrl, landingUrl, "WIN", decidedAt);
    }
}
