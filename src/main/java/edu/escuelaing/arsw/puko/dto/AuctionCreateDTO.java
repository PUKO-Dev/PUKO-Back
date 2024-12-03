package edu.escuelaing.arsw.puko.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;

// DTOs
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuctionCreateDTO {
    private Long articleId;
    private LocalDateTime startTime;
    private Duration duration;

    public AuctionCreateDTO(Long articleId) {
        this.articleId = articleId;
    }
}
