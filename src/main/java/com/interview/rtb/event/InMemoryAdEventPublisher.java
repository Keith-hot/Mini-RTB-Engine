package com.interview.rtb.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class InMemoryAdEventPublisher implements AdEventPublisher {

    private static final int DEFAULT_MAX_EVENTS = 500;

    private final ConcurrentLinkedDeque<AdEvent> events = new ConcurrentLinkedDeque<>();
    private final ApplicationEventPublisher springPublisher;
    private final int maxEvents;

    public InMemoryAdEventPublisher() {
        this(null, DEFAULT_MAX_EVENTS);
    }

    public InMemoryAdEventPublisher(int maxEvents) {
        this(null, maxEvents);
    }

    @Autowired
    public InMemoryAdEventPublisher(ApplicationEventPublisher springPublisher) {
        this(springPublisher, DEFAULT_MAX_EVENTS);
    }

    private InMemoryAdEventPublisher(ApplicationEventPublisher springPublisher, int maxEvents) {
        this.springPublisher = springPublisher;
        this.maxEvents = maxEvents;
    }

    @Override
    public void publish(AdEvent event) {
        events.addLast(event);
        while (events.size() > maxEvents) {
            events.pollFirst();
        }
        if (springPublisher != null) {
            springPublisher.publishEvent(event);
        }
    }

    public List<AdEvent> events() {
        return List.copyOf(new ArrayList<>(events));
    }

    public void clear() {
        events.clear();
    }
}
