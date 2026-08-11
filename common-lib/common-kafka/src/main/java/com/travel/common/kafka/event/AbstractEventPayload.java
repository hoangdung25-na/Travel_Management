package com.travel.common.kafka.event;

import java.time.Instant;
import java.util.UUID;

public abstract class AbstractEventPayload {
    private String eventId;
    private String eventType;
    private Instant timestamp;

    protected AbstractEventPayload() {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = this.getClass().getSimpleName();
        this.timestamp = Instant.now();
    }

    protected AbstractEventPayload(String eventId, String eventType) {
        this.eventId = eventId != null ? eventId : UUID.randomUUID().toString();
        this.eventType = eventType != null ? eventType : this.getClass().getSimpleName();
        this.timestamp = Instant.now();
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
