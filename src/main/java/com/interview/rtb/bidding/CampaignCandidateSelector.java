package com.interview.rtb.bidding;

import com.interview.rtb.campaign.Campaign;
import com.interview.rtb.campaign.CampaignReadModel;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class CampaignCandidateSelector {

    private final CampaignReadModel campaignReadModel;

    public CampaignCandidateSelector(CampaignReadModel campaignReadModel) {
        this.campaignReadModel = campaignReadModel;
    }

    public List<Campaign> rankedCandidates(BidRequest request) {
        return campaignReadModel.candidatesFor(request).stream()
                .sorted(Comparator.comparing(Campaign::bidPrice).reversed())
                .toList();
    }
}
