package com.interview.rtb.config;

import com.interview.rtb.admission.InMemoryCampaignAdmissionStore;
import com.interview.rtb.campaign.Campaign;
import com.interview.rtb.campaign.CampaignStatus;
import com.interview.rtb.campaign.Creative;
import com.interview.rtb.event.AdEvent;
import com.interview.rtb.event.ClickAttributionStore;
import com.interview.rtb.event.InMemoryAdEventPublisher;
import com.interview.rtb.metrics.MetricsRecorder;
import com.interview.rtb.metrics.MetricsSnapshot;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryDemoStateResetterTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-05-31T10:00:00Z"), ZoneOffset.UTC);

    @Test
    void resetsAdmissionEventsAndMetrics() {
        InMemoryCampaignAdmissionStore admissionStore = new InMemoryCampaignAdmissionStore(clock);
        InMemoryAdEventPublisher eventPublisher = new InMemoryAdEventPublisher();
        ClickAttributionStore clickAttributionStore = new ClickAttributionStore();
        MetricsRecorder metricsRecorder = new MetricsRecorder(clock);
        InMemoryDemoStateResetter resetter = new InMemoryDemoStateResetter(admissionStore, eventPublisher, clickAttributionStore, metricsRecorder);
        Campaign campaign = campaign();

        admissionStore.tryAdmit("user-1", campaign);
        AdEvent impression = AdEvent.impression("req-1", "user-1", campaign.id(), clock.instant());
        eventPublisher.publish(impression);
        clickAttributionStore.record(impression);
        metricsRecorder.recordBid(12, true);
        metricsRecorder.recordImpression(campaign.id());

        resetter.reset();

        MetricsSnapshot snapshot = metricsRecorder.snapshot();
        assertThat(eventPublisher.events()).isEmpty();
        assertThat(snapshot.totalBidRequests()).isZero();
        assertThat(snapshot.impressions()).isZero();
        assertThat(admissionStore.tryAdmit("user-1", campaign)).isTrue();
        assertThat(clickAttributionStore.admitClick("req-1", "user-1", campaign.id()))
                .isEqualTo(ClickAttributionStore.ClickAdmission.UNKNOWN_IMPRESSION);
    }

    private Campaign campaign() {
        return new Campaign(
                1L,
                "Demo",
                CampaignStatus.ACTIVE,
                "slot-home",
                "HK",
                Set.of("saas"),
                new BigDecimal("2.00"),
                new BigDecimal("2.00"),
                1,
                new Creative(10L, "https://example.com/ad.png", "https://example.com")
        );
    }
}
