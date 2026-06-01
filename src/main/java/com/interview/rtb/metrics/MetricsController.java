package com.interview.rtb.metrics;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final MetricsRecorder metricsRecorder;

    public MetricsController(MetricsRecorder metricsRecorder) {
        this.metricsRecorder = metricsRecorder;
    }

    @GetMapping
    public MetricsSnapshot metrics() {
        return metricsRecorder.snapshot();
    }
}
