package com.interview.rtb.campaign;

import com.interview.rtb.bidding.BidRequest;

import java.math.BigDecimal;
import java.util.Set;

public record Campaign(
        long id,
        String name,
        CampaignStatus status,
        String placementId,
        String country,
        Set<String> targetSegments,
        BigDecimal bidPrice,
        BigDecimal remainingBudget,
        int frequencyCapPerUser,
        Creative creative
) {
    public boolean matches(BidRequest request) {
        return status == CampaignStatus.ACTIVE
                && placementId.equals(request.placementId())
                && country.equalsIgnoreCase(request.country())
                && request.userSegments().stream().anyMatch(targetSegments::contains);
    }

    public boolean hasBudgetForBid() {
        return remainingBudget.compareTo(bidPrice) >= 0;
    }
}
