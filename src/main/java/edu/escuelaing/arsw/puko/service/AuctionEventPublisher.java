package edu.escuelaing.arsw.puko.service;

import com.azure.messaging.webpubsub.WebPubSubServiceClient;
import com.azure.messaging.webpubsub.WebPubSubServiceClientBuilder;
import com.azure.messaging.webpubsub.models.WebPubSubContentType;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.escuelaing.arsw.puko.exception.EventPublisherException;
import edu.escuelaing.arsw.puko.model.AuctionEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuctionEventPublisher {
    private final WebPubSubServiceClient webPubSubClient;
    private final ObjectMapper objectMapper;



    public AuctionEventPublisher(@Value("${webpubsub.connection-string}") String connectionString) {
        // Construir el cliente del servicio WebPubSub
        this.webPubSubClient = new WebPubSubServiceClientBuilder()
                .connectionString(connectionString)
                .hub("puko")
                .buildClient();
        this.objectMapper = new ObjectMapper();
    }
    public AuctionEventPublisher(WebPubSubServiceClient webPubSubClient, ObjectMapper objectMapper) {
        this.webPubSubClient = webPubSubClient;
        this.objectMapper = objectMapper;
    }


    public void publishAuctionEvent(Long auctionId, String eventType, Object eventData) {
        AuctionEvent event = new AuctionEvent(eventType, eventData);
        webPubSubClient.sendToGroup("auction-" + auctionId, serializeEvent(event), WebPubSubContentType.APPLICATION_JSON);
    }

    public void publishAuctionAvailableEvent(String eventType, Object eventData) {
        AuctionEvent event = new AuctionEvent(eventType, eventData);
        webPubSubClient.sendToGroup("auctions", serializeEvent(event), WebPubSubContentType.APPLICATION_JSON);
    }

    public void publishRemainingTimeEvent(Long auctionId, String eventType, Long remainingTime) {
        AuctionEvent event = new AuctionEvent(eventType, remainingTime);
        webPubSubClient.sendToGroup("auction-" + auctionId + "-time", serializeEvent(event), WebPubSubContentType.APPLICATION_JSON);
    }

    // Método para serializar el evento a JSON
    private String serializeEvent(AuctionEvent event) {
        try {
            return new ObjectMapper().writeValueAsString(event);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new EventPublisherException("Error serializing AuctionEvent");
        }
    }
}

