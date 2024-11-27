package edu.escuelaing.arsw.puko.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EventBidDTO {
    private Long auctionId;
    private double amount;

}
