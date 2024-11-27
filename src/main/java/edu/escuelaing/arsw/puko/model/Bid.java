package edu.escuelaing.arsw.puko.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private double amount;

    private LocalDateTime bidTime;

    @Override
    public String toString() {
        return "Bid{" +
                "id=" + id +
                ", auction=" + auction +
                ", user=" + user +
                ", amount=" + amount +
                ", bidTime=" + bidTime +
                '}';
    }
}