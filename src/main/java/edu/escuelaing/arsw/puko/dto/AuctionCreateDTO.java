package edu.escuelaing.arsw.puko.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;

// DTOs
@Getter
@Setter
public class AuctionCreateDTO {
    private Long articleId;
    private LocalDateTime startTime;
    private Duration duration;
}
