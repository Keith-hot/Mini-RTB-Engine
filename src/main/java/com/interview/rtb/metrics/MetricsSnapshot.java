package com.interview.rtb.metrics;

import java.time.Instant;
import java.util.List;

public record MetricsSnapshot(
        long totalBidRequests,
        long matchedBidRequests,
        double matchRate,
        double averageLatencyMs,
        long p95LatencyMs,
        long p99LatencyMs,
        long recentBidRequests,
        double qpsLast60s,
        long impressions,
        long clicks,
        double ctr,
        List<CampaignMetric> topCampaigns,
        Instant capturedAt
) {
}
