package com.interview.rtb.bidding;

import com.interview.rtb.admission.InMemoryCampaignAdmissionStore;
import com.interview.rtb.campaign.Campaign;
import com.interview.rtb.campaign.CampaignStatus;
import com.interview.rtb.campaign.Creative;
import com.interview.rtb.campaign.InMemoryCampaignCatalog;
import com.interview.rtb.event.InMemoryAdEventPublisher;
import com.interview.rtb.event.AdEventType;
import com.interview.rtb.metrics.MetricsRecorder;
import com.interview.rtb.metrics.MetricsSnapshot;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BidDecisionServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-05-31T10:00:00Z"), ZoneOffset.UTC);

    @Test
    void selectsHighestEligibleCampaignAndPublishesImpressionEvent() {
        Campaign lowBid = campaign(1L, "Scale SaaS", "slot-home", "HK", Set.of("saas"), "2.40", "100.00", 5);
        Campaign highBid = campaign(2L, "AI Commerce", "slot-home", "HK", Set.of("saas", "ecommerce"), "4.20", "100.00", 5);
        Campaign wrongSlot = campaign(3L, "Gaming", "slot-sidebar", "HK", Set.of("saas"), "9.00", "100.00", 5);
        InMemoryAdEventPublisher publisher = new InMemoryAdEventPublisher();
        MetricsRecorder metrics = new MetricsRecorder(clock);
        BidDecisionService service = service(List.of(lowBid, highBid, wrongSlot), publisher, metrics);

        BidResponse response = service.decide(new BidRequest("req-1", "user-7", "slot-home", "mobile", "HK", Set.of("saas")));

        assertThat(response.matched()).isTrue();
        assertThat(response.campaignId()).isEqualTo(2L);
        assertThat(response.creativeId()).isEqualTo(20L);
        assertThat(response.bidPrice()).isEqualByComparingTo("4.20");
        assertThat(publisher.events()).hasSize(1);
        assertThat(publisher.events().get(0).type()).isEqualTo(AdEventType.IMPRESSION);
        assertThat(metrics.snapshot()).extracting(MetricsSnapshot::totalBidRequests, MetricsSnapshot::matchedBidRequests)
                .containsExactly(1L, 1L);
    }

    @Test
    void filtersCampaignsWithInsufficientBudget() {
        Campaign depleted = campaign(1L, "No Budget", "slot-home", "HK", Set.of("saas"), "4.00", "3.99", 5);
        BidDecisionService service = service(List.of(depleted), new InMemoryAdEventPublisher(), new MetricsRecorder(clock));

        BidResponse response = service.decide(new BidRequest("req-2", "user-1", "slot-home", "desktop", "HK", Set.of("saas")));

        assertThat(response.matched()).isFalse();
        assertThat(response.reason()).isEqualTo("NO_ELIGIBLE_CAMPAIGN");
    }

    @Test
    void spendsBudgetAtomicallyAfterWinningBid() {
        Campaign campaign = campaign(1L, "Small Budget", "slot-home", "HK", Set.of("saas"), "3.00", "5.00", 5);
        BidDecisionService service = service(List.of(campaign), new InMemoryAdEventPublisher(), new MetricsRecorder(clock));

        BidResponse first = service.decide(new BidRequest("req-budget-1", "user-1", "slot-home", "desktop", "HK", Set.of("saas")));
        BidResponse second = service.decide(new BidRequest("req-budget-2", "user-2", "slot-home", "desktop", "HK", Set.of("saas")));

        assertThat(first.matched()).isTrue();
        assertThat(second.matched()).isFalse();
        assertThat(second.reason()).isEqualTo("NO_ELIGIBLE_CAMPAIGN");
    }

    @Test
    void filtersCampaignsWhenFrequencyCapIsReached() {
        Campaign campaign = campaign(1L, "Capped", "slot-home", "HK", Set.of("saas"), "2.50", "100.00", 1);
        BidDecisionService service = service(List.of(campaign), new InMemoryAdEventPublisher(), new MetricsRecorder(clock));

        BidResponse first = service.decide(new BidRequest("req-3a", "user-1", "slot-home", "desktop", "HK", Set.of("saas")));
        BidResponse second = service.decide(new BidRequest("req-3b", "user-1", "slot-home", "desktop", "HK", Set.of("saas")));

        assertThat(first.matched()).isTrue();
        assertThat(second.matched()).isFalse();
        assertThat(second.reason()).isEqualTo("NO_ELIGIBLE_CAMPAIGN");
    }

    @Test
    void cappedUserDoesNotConsumeBudgetForOtherUsers() {
        Campaign campaign = campaign(1L, "Capped", "slot-home", "HK", Set.of("saas"), "3.00", "6.00", 1);
        BidDecisionService service = service(List.of(campaign), new InMemoryAdEventPublisher(), new MetricsRecorder(clock));

        BidResponse first = service.decide(new BidRequest("req-cap-budget-1", "user-1", "slot-home", "desktop", "HK", Set.of("saas")));
        BidResponse capped = service.decide(new BidRequest("req-cap-budget-2", "user-1", "slot-home", "desktop", "HK", Set.of("saas")));
        BidResponse otherUser = service.decide(new BidRequest("req-cap-budget-3", "user-2", "slot-home", "desktop", "HK", Set.of("saas")));

        assertThat(first.matched()).isTrue();
        assertThat(capped.matched()).isFalse();
        assertThat(otherUser.matched()).isTrue();
    }

    @Test
    void triesNextCampaignWhenHighestBidCannotBeAdmitted() {
        Campaign highBid = campaign(1L, "High Capped", "slot-home", "HK", Set.of("saas"), "5.00", "100.00", 1);
        Campaign fallback = campaign(2L, "Fallback", "slot-home", "HK", Set.of("saas"), "3.00", "100.00", 5);
        BidDecisionService service = service(List.of(highBid, fallback), new InMemoryAdEventPublisher(), new MetricsRecorder(clock));

        BidResponse first = service.decide(new BidRequest("req-fallback-1", "user-1", "slot-home", "desktop", "HK", Set.of("saas")));
        BidResponse second = service.decide(new BidRequest("req-fallback-2", "user-1", "slot-home", "desktop", "HK", Set.of("saas")));

        assertThat(first.campaignId()).isEqualTo(1L);
        assertThat(second.matched()).isTrue();
        assertThat(second.campaignId()).isEqualTo(2L);
    }

    private BidDecisionService service(List<Campaign> campaigns, InMemoryAdEventPublisher publisher, MetricsRecorder metrics) {
        return new BidDecisionService(
                new CampaignCandidateSelector(new InMemoryCampaignCatalog(campaigns)),
                new InMemoryCampaignAdmissionStore(clock),
                publisher,
                metrics,
                clock
        );
    }

    private Campaign campaign(long id, String name, String placement, String country, Set<String> segments,
                              String bid, String remainingBudget, int frequencyCap) {
        return new Campaign(
                id,
                name,
                CampaignStatus.ACTIVE,
                placement,
                country,
                segments,
                new BigDecimal(bid),
                new BigDecimal(remainingBudget),
                frequencyCap,
                new Creative(id * 10, "https://example.com/" + id + ".png", "https://landing.example.com/" + id)
        );
    }

}
