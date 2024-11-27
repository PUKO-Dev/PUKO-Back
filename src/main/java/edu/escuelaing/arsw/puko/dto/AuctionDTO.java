package edu.escuelaing.arsw.puko.dto;

import edu.escuelaing.arsw.puko.model.Auction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
public class AuctionDTO {
    private Long id;
    private Long articleId;
    private Long creatorId;
    private Long duration;
    private String startTime;
    private Auction.AuctionStatus status;
    private List<BidDTO> bidRanking;

    public static AuctionDTO fromAuction(Auction auctionCreated) {
        return new AuctionDTO(
                auctionCreated.getId(),
                auctionCreated.getArticle().getId(),
                auctionCreated.getCreator().getId(),
                auctionCreated.getDuration().toMillis(),
                auctionCreated.getStartTime().toString(),
                auctionCreated.getStatus(),
                auctionCreated.getTopBids().stream()
                        .map(entry -> new BidDTO(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList())
        );
    }
    @Getter
    @Setter
    @AllArgsConstructor
    public static class BidDTO {
        private String userId;
        private Double amount;
    }
}

