package com.interview.rtb.metrics;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsRecorderTest {

    @Test
    void recordsBidLatencyMatchRateAndCtr() {
        MetricsRecorder recorder = new MetricsRecorder(Clock.fixed(Instant.parse("2026-05-31T10:00:00Z"), ZoneOffset.UTC));

        recorder.recordBid(12, true);
        recorder.recordBid(28, false);
        recorder.recordImpression(1L);
        recorder.recordImpression(1L);
        recorder.recordClick(1L);

        MetricsSnapshot snapshot = recorder.snapshot();

        assertThat(snapshot.totalBidRequests()).isEqualTo(2);
        assertThat(snapshot.matchedBidRequests()).isEqualTo(1);
        assertThat(snapshot.matchRate()).isEqualTo(0.5);
        assertThat(snapshot.averageLatencyMs()).isEqualTo(20.0);
        assertThat(snapshot.p95LatencyMs()).isEqualTo(28);
        assertThat(snapshot.p99LatencyMs()).isEqualTo(28);
        assertThat(snapshot.recentBidRequests()).isEqualTo(2);
        assertThat(snapshot.qpsLast60s()).isEqualTo(2.0 / 60.0);
        assertThat(snapshot.impressions()).isEqualTo(2);
        assertThat(snapshot.clicks()).isEqualTo(1);
        assertThat(snapshot.ctr()).isEqualTo(0.5);
        assertThat(snapshot.topCampaigns()).hasSize(1);
        assertThat(snapshot.topCampaigns().get(0).campaignId()).isEqualTo(1L);
    }

    @Test
    void recordsTailLatencyPercentiles() {
        MetricsRecorder recorder = new MetricsRecorder(Clock.fixed(Instant.parse("2026-05-31T10:00:00Z"), ZoneOffset.UTC));

        recorder.recordBid(8, true);
        recorder.recordBid(12, true);
        recorder.recordBid(18, true);
        recorder.recordBid(24, true);
        recorder.recordBid(47, false);
        recorder.recordBid(96, false);

        MetricsSnapshot snapshot = recorder.snapshot();

        assertThat(snapshot.p95LatencyMs()).isEqualTo(96);
        assertThat(snapshot.p99LatencyMs()).isEqualTo(96);
        assertThat(snapshot.matchRate()).isEqualTo(4.0 / 6.0);
    }

    @Test
    void resetClearsRecordedMetrics() {
        MetricsRecorder recorder = new MetricsRecorder(Clock.fixed(Instant.parse("2026-05-31T10:00:00Z"), ZoneOffset.UTC));
        recorder.recordBid(12, true);
        recorder.recordImpression(1L);
        recorder.recordClick(1L);

        recorder.reset();

        MetricsSnapshot snapshot = recorder.snapshot();
        assertThat(snapshot.totalBidRequests()).isZero();
        assertThat(snapshot.matchedBidRequests()).isZero();
        assertThat(snapshot.impressions()).isZero();
        assertThat(snapshot.clicks()).isZero();
        assertThat(snapshot.topCampaigns()).isEmpty();
    }
}
