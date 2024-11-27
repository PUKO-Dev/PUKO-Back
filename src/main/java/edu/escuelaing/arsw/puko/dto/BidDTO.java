package edu.escuelaing.arsw.puko.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BidDTO {
    private double amount;

    @Override
    public String toString() {
        return "BidDTO{" +
                "amount=" + amount +
                '}';
    }
}
