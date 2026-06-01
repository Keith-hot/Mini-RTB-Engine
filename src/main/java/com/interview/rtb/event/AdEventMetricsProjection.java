package com.interview.rtb.event;

import com.interview.rtb.metrics.MetricsRecorder;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AdEventMetricsProjection {

    private final MetricsRecorder metricsRecorder;

    public AdEventMetricsProjection(MetricsRecorder metricsRecorder) {
        this.metricsRecorder = metricsRecorder;
    }

    @EventListener
    public void record(AdEvent event) {
        switch (event.type()) {
            case IMPRESSION -> metricsRecorder.recordImpression(event.campaignId());
            case CLICK -> metricsRecorder.recordClick(event.campaignId());
        }
    }
}
