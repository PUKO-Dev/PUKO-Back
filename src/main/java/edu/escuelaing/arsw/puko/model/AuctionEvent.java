package edu.escuelaing.arsw.puko.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuctionEvent {
    private String eventType;
    private Object eventData;

    public AuctionEvent(String eventType, Object eventData) {
        this.eventType = eventType;
        this.eventData = eventData;
    }

    // Getters y setters
}

