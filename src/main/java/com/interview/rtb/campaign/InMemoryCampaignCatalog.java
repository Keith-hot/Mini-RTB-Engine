package com.interview.rtb.campaign;

import com.interview.rtb.bidding.BidRequest;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Repository
public class InMemoryCampaignCatalog implements CampaignReadModel, CampaignListing {

    private final List<Campaign> campaigns;

    public InMemoryCampaignCatalog() {
        this(seedCampaigns());
    }

    public InMemoryCampaignCatalog(List<Campaign> campaigns) {
        this.campaigns = List.copyOf(campaigns);
    }

    @Override
    public List<Campaign> activeCampaigns() {
        return campaigns;
    }

    @Override
    public List<Campaign> candidatesFor(BidRequest request) {
        return campaigns.stream()
                .filter(campaign -> campaign.matches(request))
                .filter(Campaign::hasBudgetForBid)
                .toList();
    }

    private static List<Campaign> seedCampaigns() {
        return List.of(
                new Campaign(101L, "AI Commerce Retargeting", CampaignStatus.ACTIVE, "slot-home", "HK",
                        Set.of("ecommerce", "saas"), new BigDecimal("4.80"), new BigDecimal("2500.00"), 4,
                        new Creative(1001L, "https://images.unsplash.com/photo-1551288049-bebda4e38f71", "https://cyberlabo.io")),
                new Campaign(102L, "Fintech Growth", CampaignStatus.ACTIVE, "slot-home", "HK",
                        Set.of("finance", "founder"), new BigDecimal("3.60"), new BigDecimal("1800.00"), 3,
                        new Creative(1002L, "https://images.unsplash.com/photo-1554224155-6726b3ff858f", "https://cyberlabo.io")),
                new Campaign(103L, "Developer Cloud Tools", CampaignStatus.ACTIVE, "slot-sidebar", "SG",
                        Set.of("developer", "cloud"), new BigDecimal("5.20"), new BigDecimal("3200.00"), 5,
                        new Creative(1003L, "https://images.unsplash.com/photo-1515879218367-8466d910aaa4", "https://cyberlabo.io"))
        );
    }
}
