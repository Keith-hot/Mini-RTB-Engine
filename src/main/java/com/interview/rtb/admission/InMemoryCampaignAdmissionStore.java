package com.interview.rtb.admission;

import com.interview.rtb.campaign.Campaign;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryCampaignAdmissionStore implements CampaignAdmissionStore {

    private final Clock clock;
    private final Map<Long, BigDecimal> budgets = new HashMap<>();
    private final Map<String, Integer> frequencyCounts = new HashMap<>();

    public InMemoryCampaignAdmissionStore(Clock clock) {
        this.clock = clock;
    }

    @Override
    public synchronized boolean tryAdmit(String userId, Campaign campaign) {
        BigDecimal currentBudget = budgetFor(campaign);
        if (currentBudget.compareTo(campaign.bidPrice()) < 0) {
            return false;
        }

        String frequencyKey = frequencyKey(userId, campaign.id());
        int currentFrequency = frequencyCounts.getOrDefault(frequencyKey, 0);
        if (currentFrequency >= campaign.frequencyCapPerUser()) {
            return false;
        }

        budgets.put(campaign.id(), currentBudget.subtract(campaign.bidPrice()));
        frequencyCounts.put(frequencyKey, currentFrequency + 1);
        return true;
    }

    public synchronized void reset() {
        budgets.clear();
        frequencyCounts.clear();
    }

    private BigDecimal budgetFor(Campaign campaign) {
        return budgets.computeIfAbsent(campaign.id(), ignored -> campaign.remainingBudget());
    }

    private String frequencyKey(String userId, long campaignId) {
        return LocalDate.now(clock) + ":" + userId + ":" + campaignId;
    }
}
