package com.interview.rtb.event;

import com.interview.rtb.metrics.MetricsRecorder;
import com.interview.rtb.metrics.MetricsSnapshot;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class AdEventMetricsProjectionTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-05-31T10:00:00Z"), ZoneOffset.UTC);

    @Test
    void recordsImpressionsAndClicksFromPublishedAdEvents() {
        MetricsRecorder metricsRecorder = new MetricsRecorder(clock);
        AdEventMetricsProjection projection = new AdEventMetricsProjection(metricsRecorder);
        InMemoryAdEventPublisher publisher = new InMemoryAdEventPublisher(event -> projection.record((AdEvent) event));

        publisher.publish(AdEvent.impression("req-1", "user-1", 101L, clock.instant()));
        publisher.publish(AdEvent.click("req-2", "user-1", 101L, clock.instant()));

        MetricsSnapshot snapshot = metricsRecorder.snapshot();
        assertThat(snapshot.impressions()).isEqualTo(1);
        assertThat(snapshot.clicks()).isEqualTo(1);
        assertThat(snapshot.ctr()).isEqualTo(1.0);
        assertThat(snapshot.topCampaigns()).hasSize(1);
        assertThat(snapshot.topCampaigns().get(0).campaignId()).isEqualTo(101L);
    }
}
