package edu.escuelaing.arsw.puko.service;

import edu.escuelaing.arsw.puko.dto.AuctionDTO;
import edu.escuelaing.arsw.puko.dto.EventBidDTO;
import edu.escuelaing.arsw.puko.exception.AuctionException;
import edu.escuelaing.arsw.puko.exception.AuctionNotFoundException;
import edu.escuelaing.arsw.puko.model.Auction;
import edu.escuelaing.arsw.puko.model.User;
import edu.escuelaing.arsw.puko.repository.AuctionRepository;
import jakarta.persistence.OptimisticLockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuctionService {

    private final String AUCTION_STARTED = "AUCTION_STARTED";
    private final String AUCTION_FINALIZED = "AUCTION_FINALIZED";


    private AuctionRepository auctionRepository;

    private AuctionEventPublisher auctionEventPublisher;

    @Autowired
    public AuctionService(AuctionRepository auctionRepository, AuctionEventPublisher auctionEventPublisher) {
        this.auctionRepository = auctionRepository;
        this.auctionEventPublisher = auctionEventPublisher;
    }

    @Transactional
    public Auction save(Auction auction) {
        Auction savedAuction = auctionRepository.save(auction);
        savedAuction.initializeBidRanking();
        auctionEventPublisher.publishAuctionAvailableEvent("AUCTION_CREATED", AuctionDTO.fromAuction(savedAuction));
        return savedAuction;
    }

    @Transactional
    public boolean registerUserForAuction(Long auctionId, User user) {
        Optional<Auction> auctionOpt = findById(auctionId);
        if (auctionOpt.isPresent()) {
            Auction auction = auctionOpt.get();
            boolean registered = auction.registerUser(user);
            if (registered) {
                auction.initializeBidRanking();
                auctionRepository.save(auction);
                auctionEventPublisher.publishAuctionEvent(auction.getId(), "USER_REGISTERED", auction.getTopBids());
            }
            return registered;
        }
        return false;
    }

    @Transactional
    public boolean placeBid(Long auctionId, User user, double amount) {
        try{
            Optional<Auction> auctionOpt = Optional.of(auctionRepository.findAuctionForUpdate(auctionId).orElseThrow());

            Auction auction = auctionOpt.get();
            if (auction.getStatus() != Auction.AuctionStatus.ACTIVE) {
                return false;
            }
            boolean bidPlaced = auction.placeBid(user, amount);
            if (bidPlaced) {
                auctionRepository.save(auction);
                auction.initializeBidRanking();

                auctionRepository.save(auction);

                auctionEventPublisher.publishAuctionEvent(auction.getId(), "BID_PLACED", amount);
                auctionEventPublisher.publishAuctionAvailableEvent("NEW_TOP_BID", new EventBidDTO(auction.getId(), amount));
                auctionEventPublisher.publishAuctionEvent(auction.getId(), "RANKING_UPDATED", auction.getTopBids());
            }
            return bidPlaced;
        }catch (Exception e){
            return false;
        }
    }

    @Transactional(readOnly = true)
    public List<Map.Entry<String, Double>> getTopBids(Long auctionId) {
        return findById(auctionId)
                .map(auction -> {
                    auction.initializeBidRanking();
                    return auction.getTopBids();
                })
                .orElse(Collections.emptyList());
    }

    @Transactional(readOnly = true)
    public Duration getRemainingTime(Long auctionId) {
        return findById(auctionId)
                .map(Auction::getRemainingTime)
                .orElse(Duration.ZERO);
    }

    @Transactional(readOnly = true)
    public Optional<Auction> findById(Long id) {
        Optional<Auction> auctionOpt = auctionRepository.findById(id);
        auctionOpt.ifPresent(Auction::initializeBidRanking);
        return auctionOpt;
    }

    @Transactional(readOnly = true)
    public List<Auction> findActiveAuctions() {
        List<Auction> auctions = auctionRepository.findByStatus(Auction.AuctionStatus.ACTIVE);
        auctions.forEach(Auction::initializeBidRanking);
        return auctions;
    }

    @Transactional(readOnly = true)
    public List<Auction> findByCreator(User creator) {
        List<Auction> auctions = auctionRepository.findByCreator(creator);
        auctions.forEach(Auction::initializeBidRanking);
        return auctions;
    }

    @Transactional(readOnly = true)
    public List<Auction> findByRegisteredUser(User user) {
        List<Auction> auctions = auctionRepository.findByRegisteredUser(user);
        auctions.forEach(Auction::initializeBidRanking);
        return auctions;
    }

    @Transactional
    public boolean startAuction(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionNotFoundException(auctionId));
        auction.initializeBidRanking();
        boolean started = auction.startAuction();
        if (started) {
            auctionRepository.save(auction);
            auctionEventPublisher.publishAuctionEvent(auction.getId(), AUCTION_STARTED, null);
            auctionEventPublisher.publishAuctionAvailableEvent(AUCTION_STARTED, auction.getId());
        }
        return started;
    }

    @Transactional
    public void finalizeAuction(Long auctionId) throws AuctionException {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionNotFoundException(auctionId));
        auction.finalizeAuction();
        auction.initializeBidRanking();
        auctionRepository.save(auction);
        auctionEventPublisher.publishAuctionEvent(auction.getId(), AUCTION_FINALIZED, auction.getWinner());
        auctionEventPublisher.publishAuctionAvailableEvent(AUCTION_FINALIZED, auction.getId());
    }

    @Transactional(readOnly = true)
    public List<Auction> findAvailableAuctions() {
        List<Auction> auctions = auctionRepository.findAvailableAuctions();
        auctions.forEach(Auction::initializeBidRanking);
        return auctions;
    }

    @Transactional(readOnly = true)
    public Set<User> getRegisteredUsers(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionNotFoundException(auctionId));
        auction.initializeBidRanking();
        return auction.getRegisteredUsers();
    }

    @Transactional(readOnly = true)
    public User getWinner(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionNotFoundException(auctionId));
        auction.initializeBidRanking();
        return auction.getWinner();
    }

    @Scheduled(fixedRate = 1000) // Publicar cada segundo
    @Transactional
    public void publishRemainingTimeForActiveAuctions() {
        // Obtener todas las subastas activas
        List<Auction> activeAuctions = auctionRepository.findByStatus(Auction.AuctionStatus.ACTIVE);

        for (Auction auction : activeAuctions) {
            auction.initializeBidRanking();  // Inicializa la clasificación de las pujas

            // Obtener el tiempo restante
            Duration remainingTime = auction.getRemainingTime();

            auctionEventPublisher.publishRemainingTimeEvent(auction.getId(), "REMAINING_TIME", remainingTime.toSeconds());
        }
    }


    @Scheduled(fixedRate = 1000)
    @Transactional
    public void updateAuctionStatuses() throws AuctionException {
        LocalDateTime now = LocalDateTime.now();

        // Activar subastas programadas
        List<Auction> scheduledAuctions = auctionRepository.findByStatus(Auction.AuctionStatus.SCHEDULED);
        for (Auction auction : scheduledAuctions) {
            auction.initializeBidRanking();
            if (now.isAfter(auction.getStartTime())) {
                auction.setStatus(Auction.AuctionStatus.ACTIVE);
                auctionRepository.save(auction);
                auctionEventPublisher.publishAuctionEvent(auction.getId(), AUCTION_STARTED, null);
                auctionEventPublisher.publishAuctionAvailableEvent(AUCTION_STARTED, auction.getId());
            }
        }

        // Finalizar subastas activas
        List<Auction> activeAuctions = auctionRepository.findByStatus(Auction.AuctionStatus.ACTIVE);
        for (Auction auction : activeAuctions) {
            auction.initializeBidRanking();
            if (now.isAfter(auction.getStartTime().plus(auction.getDuration()))) {
                auction.finalizeAuction();
                auctionRepository.save(auction);
                auctionEventPublisher.publishAuctionEvent(auction.getId(), AUCTION_FINALIZED, auction.getWinner());
                auctionEventPublisher.publishAuctionAvailableEvent(AUCTION_FINALIZED, auction.getId());
            }
        }
    }
}