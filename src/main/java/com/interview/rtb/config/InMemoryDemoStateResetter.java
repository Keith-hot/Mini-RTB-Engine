package com.interview.rtb.config;

import com.interview.rtb.admission.InMemoryCampaignAdmissionStore;
import com.interview.rtb.event.InMemoryAdEventPublisher;
import com.interview.rtb.metrics.MetricsRecorder;
import org.springframework.stereotype.Component;

@Component
public class InMemoryDemoStateResetter implements DemoStateResetter {

    private final InMemoryCampaignAdmissionStore admissionStore;
    private final InMemoryAdEventPublisher eventPublisher;
    private final MetricsRecorder metricsRecorder;

    public InMemoryDemoStateResetter(InMemoryCampaignAdmissionStore admissionStore,
                                     InMemoryAdEventPublisher eventPublisher,
                                     MetricsRecorder metricsRecorder) {
        this.admissionStore = admissionStore;
        this.eventPublisher = eventPublisher;
        this.metricsRecorder = metricsRecorder;
    }

    @Override
    public void reset() {
        admissionStore.reset();
        eventPublisher.clear();
        metricsRecorder.reset();
    }
}
