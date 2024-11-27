package edu.escuelaing.arsw.puko.exception;

public class AuctionNotFoundException extends RuntimeException {
    public AuctionNotFoundException(Long auctionId) {
        super("Auction not found with ID: " + auctionId);
    }

    public AuctionNotFoundException(String message) {
        super(message);
    }

}