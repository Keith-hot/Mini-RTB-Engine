package com.interview.rtb.campaign;

import com.interview.rtb.bidding.BidRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryCampaignCatalogTest {

    @Test
    void listingReturnsActiveCampaignsForDashboard() {
        InMemoryCampaignCatalog catalog = new InMemoryCampaignCatalog(List.of(
                campaign(1L, "slot-home", "HK", Set.of("saas"), "3.00", "100.00"),
                campaign(2L, "slot-sidebar", "SG", Set.of("developer"), "5.00", "100.00")
        ));

        assertThat(catalog.activeCampaigns()).extracting(Campaign::id)
                .containsExactly(1L, 2L);
    }

    @Test
    void readModelReturnsRequestShapedCandidatesForBidding() {
        InMemoryCampaignCatalog catalog = new InMemoryCampaignCatalog(List.of(
                campaign(1L, "slot-home", "HK", Set.of("saas"), "3.00", "100.00"),
                campaign(2L, "slot-home", "HK", Set.of("finance"), "5.00", "100.00"),
                campaign(3L, "slot-sidebar", "HK", Set.of("saas"), "9.00", "100.00"),
                campaign(4L, "slot-home", "HK", Set.of("saas"), "4.00", "3.99")
        ));

        List<Campaign> candidates = catalog.candidatesFor(
                new BidRequest("req-1", "user-1", "slot-home", "mobile", "HK", Set.of("saas"))
        );

        assertThat(candidates).extracting(Campaign::id)
                .containsExactly(1L);
    }

    private Campaign campaign(long id, String placement, String country, Set<String> segments,
                              String bid, String remainingBudget) {
        return new Campaign(
                id,
                "Campaign " + id,
                CampaignStatus.ACTIVE,
                placement,
                country,
                segments,
                new BigDecimal(bid),
                new BigDecimal(remainingBudget),
                5,
                new Creative(id * 10, "https://example.com/" + id + ".png", "https://landing.example.com/" + id)
        );
    }
}
