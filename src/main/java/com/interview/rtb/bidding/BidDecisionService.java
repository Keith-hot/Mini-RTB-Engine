package com.interview.rtb.bidding;

import com.interview.rtb.admission.CampaignAdmissionStore;
import com.interview.rtb.campaign.Campaign;
import com.interview.rtb.event.AdEvent;
import com.interview.rtb.event.AdEventPublisher;
import com.interview.rtb.metrics.MetricsRecorder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
public class BidDecisionService {

    private final CampaignCandidateSelector campaignCandidateSelector;
    private final CampaignAdmissionStore campaignAdmissionStore;
    private final AdEventPublisher adEventPublisher;
    private final MetricsRecorder metricsRecorder;
    private final Clock clock;

    public BidDecisionService(CampaignCandidateSelector campaignCandidateSelector,
                              CampaignAdmissionStore campaignAdmissionStore,
                              AdEventPublisher adEventPublisher,
                              MetricsRecorder metricsRecorder,
                              Clock clock) {
        this.campaignCandidateSelector = campaignCandidateSelector;
        this.campaignAdmissionStore = campaignAdmissionStore;
        this.adEventPublisher = adEventPublisher;
        this.metricsRecorder = metricsRecorder;
        this.clock = clock;
    }

    public BidResponse decide(BidRequest request) {
        long started = System.nanoTime();
        Instant decidedAt = clock.instant();
        for (Campaign candidate : campaignCandidateSelector.rankedCandidates(request)) {
            if (campaignAdmissionStore.tryAdmit(request.userId(), candidate)) {
                adEventPublisher.publish(AdEvent.impression(request.requestId(), request.userId(), candidate.id(), decidedAt));
                recordLatency(started, true);
                return BidResponse.win(
                        candidate.id(),
                        candidate.creative().id(),
                        candidate.bidPrice(),
                        candidate.creative().assetUrl(),
                        candidate.creative().landingUrl(),
                        decidedAt
                );
            }
        }

        recordLatency(started, false);
        return BidResponse.noBid("NO_ELIGIBLE_CAMPAIGN", decidedAt);
    }

    private void recordLatency(long startedNanos, boolean matched) {
        long elapsedMs = Math.max(1, (System.nanoTime() - startedNanos) / 1_000_000);
        metricsRecorder.recordBid(elapsedMs, matched);
    }
}
