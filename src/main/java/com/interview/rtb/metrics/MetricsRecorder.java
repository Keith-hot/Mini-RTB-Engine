package com.interview.rtb.metrics;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayDeque;
import java.util.Deque;

@Component
public class MetricsRecorder {

    private static final int MAX_LATENCY_SAMPLES = 2_000;
    private static final long QPS_WINDOW_MILLIS = 60_000;

    private final Clock clock;
    private long totalBidRequests;
    private long matchedBidRequests;
    private long latencyMsTotal;
    private long impressions;
    private long clicks;
    private final Map<Long, Long> campaignImpressions = new HashMap<>();
    private final Map<Long, Long> campaignClicks = new HashMap<>();
    private final Deque<LatencySample> latencySamples = new ArrayDeque<>();

    public MetricsRecorder(Clock clock) {
        this.clock = clock;
    }

    public synchronized void recordBid(long latencyMs, boolean matched) {
        totalBidRequests++;
        latencyMsTotal += latencyMs;
        latencySamples.addLast(new LatencySample(clock.millis(), latencyMs));
        trimLatencySamples();
        if (matched) {
            matchedBidRequests++;
        }
    }

    public synchronized void recordImpression(long campaignId) {
        impressions++;
        campaignImpressions.merge(campaignId, 1L, Long::sum);
    }

    public synchronized void recordClick(long campaignId) {
        clicks++;
        campaignClicks.merge(campaignId, 1L, Long::sum);
    }

    public synchronized MetricsSnapshot snapshot() {
        long bids = totalBidRequests;
        long matchedBids = matchedBidRequests;
        long impressionCount = impressions;
        long clickCount = clicks;
        List<LatencySample> recentSamples = recentLatencySamples();
        List<Long> recentLatencies = recentSamples.stream()
                .map(LatencySample::latencyMs)
                .sorted()
                .toList();
        List<CampaignMetric> topCampaigns = campaignImpressions.entrySet().stream()
                .map(entry -> {
                    long campaignId = entry.getKey();
                    long campaignImpressionCount = entry.getValue();
                    long campaignClickCount = campaignClicks.getOrDefault(campaignId, 0L);
                    return new CampaignMetric(campaignId, campaignImpressionCount, campaignClickCount,
                            ratio(campaignClickCount, campaignImpressionCount));
                })
                .sorted(Comparator.comparing(CampaignMetric::impressions).reversed())
                .limit(5)
                .toList();

        return new MetricsSnapshot(
                bids,
                matchedBids,
                ratio(matchedBids, bids),
                bids == 0 ? 0 : (double) latencyMsTotal / bids,
                percentile(recentLatencies, 95),
                percentile(recentLatencies, 99),
                recentSamples.size(),
                recentSamples.size() / 60.0,
                impressionCount,
                clickCount,
                ratio(clickCount, impressionCount),
                topCampaigns,
                clock.instant()
        );
    }

    public synchronized void reset() {
        totalBidRequests = 0;
        matchedBidRequests = 0;
        latencyMsTotal = 0;
        impressions = 0;
        clicks = 0;
        campaignImpressions.clear();
        campaignClicks.clear();
        latencySamples.clear();
    }

    private double ratio(long numerator, long denominator) {
        return denominator == 0 ? 0 : (double) numerator / denominator;
    }

    private void trimLatencySamples() {
        while (latencySamples.size() > MAX_LATENCY_SAMPLES) {
            latencySamples.pollFirst();
        }
        long cutoff = clock.millis() - QPS_WINDOW_MILLIS;
        while (true) {
            LatencySample first = latencySamples.peekFirst();
            if (first == null || first.recordedAtMillis() >= cutoff) {
                return;
            }
            latencySamples.pollFirst();
        }
    }

    private List<LatencySample> recentLatencySamples() {
        long cutoff = clock.millis() - QPS_WINDOW_MILLIS;
        List<LatencySample> samples = new ArrayList<>();
        for (LatencySample sample : latencySamples) {
            if (sample.recordedAtMillis() >= cutoff) {
                samples.add(sample);
            }
        }
        return samples;
    }

    private long percentile(List<Long> sortedValues, int percentile) {
        if (sortedValues.isEmpty()) {
            return 0;
        }
        int index = (int) Math.ceil((percentile / 100.0) * sortedValues.size()) - 1;
        return sortedValues.get(Math.max(0, Math.min(index, sortedValues.size() - 1)));
    }

    private record LatencySample(long recordedAtMillis, long latencyMs) {
    }
}
