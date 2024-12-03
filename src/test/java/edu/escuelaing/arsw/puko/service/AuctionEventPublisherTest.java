package edu.escuelaing.arsw.puko.service;

import com.azure.messaging.webpubsub.WebPubSubServiceClient;
import com.azure.messaging.webpubsub.models.WebPubSubContentType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.escuelaing.arsw.puko.exception.EventPublisherException;
import edu.escuelaing.arsw.puko.model.AuctionEvent;
import edu.escuelaing.arsw.puko.service.AuctionEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AuctionEventPublisherTest {

    @Mock
    private WebPubSubServiceClient webPubSubClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuctionEventPublisher auctionEventPublisher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        auctionEventPublisher = new AuctionEventPublisher(webPubSubClient, objectMapper);
    }

    @Test
    void publishAuctionEvent_ShouldSendEvent() throws JsonProcessingException {
        Long auctionId = 1L;
        String eventType = "NEW_BID";
        Object eventData = "Test Data";
        AuctionEvent event = new AuctionEvent(eventType, eventData);
        String serializedEvent = "{\"eventType\":\"NEW_BID\",\"eventData\":\"Test Data\"}";

        when(objectMapper.writeValueAsString(event)).thenReturn(serializedEvent);

        auctionEventPublisher.publishAuctionEvent(auctionId, eventType, eventData);

        verify(webPubSubClient, times(1)).sendToGroup("auction-1", serializedEvent, WebPubSubContentType.APPLICATION_JSON);
    }
    @Test
    void publishAuctionAvailableEvent_ShouldSendEvent() throws JsonProcessingException {
        String eventType = "NEW_AUCTION";
        Object eventData = "Auction Data";
        AuctionEvent event = new AuctionEvent(eventType, eventData);
        String serializedEvent = "{\"eventType\":\"NEW_AUCTION\",\"eventData\":\"Auction Data\"}";

        when(objectMapper.writeValueAsString(event)).thenReturn(serializedEvent);

        auctionEventPublisher.publishAuctionAvailableEvent(eventType, eventData);

        verify(webPubSubClient, times(1)).sendToGroup("auctions", serializedEvent, WebPubSubContentType.APPLICATION_JSON);
    }
    @Test
    void publishRemainingTimeEvent_ShouldSendEvent() throws JsonProcessingException {
        Long auctionId = 1L;
        String eventType = "TIME_UPDATE";
        Long remainingTime = 300L;
        AuctionEvent event = new AuctionEvent(eventType, remainingTime);
        String serializedEvent = "{\"eventType\":\"TIME_UPDATE\",\"eventData\":300}";

        when(objectMapper.writeValueAsString(event)).thenReturn(serializedEvent);

        auctionEventPublisher.publishRemainingTimeEvent(auctionId, eventType, remainingTime);

        verify(webPubSubClient, times(1)).sendToGroup("auction-1-time", serializedEvent, WebPubSubContentType.APPLICATION_JSON);
    }
}
