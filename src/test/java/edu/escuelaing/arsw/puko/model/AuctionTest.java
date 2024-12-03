package edu.escuelaing.arsw.puko.model;

import edu.escuelaing.arsw.puko.exception.AuctionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuctionTest {

    private Auction auction;

    @Mock
    private User mockCreator;

    @Mock
    private User mockBidder;

    @Mock
    private Article mockArticle;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup initial values
        when(mockCreator.getId()).thenReturn(1L);
        when(mockCreator.getUsername()).thenReturn("Creator");
        when(mockArticle.isInAuction()).thenReturn(false);

        auction = new Auction(mockCreator, mockArticle, Duration.ofMinutes(10), LocalDateTime.now());
    }

    @Test
    void testStartAuction() {
        assertTrue(auction.startAuction(), "Auction should start successfully");
        assertEquals(Auction.AuctionStatus.ACTIVE, auction.getStatus(), "Auction status should be ACTIVE");
    }

    @Test
    void testRegisterUser() {
        when(mockBidder.getId()).thenReturn(2L);
        when(mockBidder.getUsername()).thenReturn("Bidder");

        assertTrue(auction.registerUser(mockBidder), "User should register successfully");
        assertTrue(auction.getRegisteredUsers().contains(mockBidder), "Registered user should be in the list");
    }

    @Test
    void testPlaceBid_Success() {
        when(mockBidder.getId()).thenReturn(2L);
        when(mockBidder.getUsername()).thenReturn("Bidder");
        when(mockBidder.getTemporaryMoney()).thenReturn(500.0);
        when(mockArticle.getInitialPrice()).thenReturn(100.0);

        auction.registerUser(mockBidder);
        auction.startAuction();

        assertTrue(auction.placeBid(mockBidder, 150.0), "Bid should be placed successfully");
        assertEquals(1, auction.getBids().size(), "There should be one bid");
    }

    @Test
    void testPlaceBid_InsufficientFunds() {
        when(mockBidder.getId()).thenReturn(2L);
        when(mockBidder.getUsername()).thenReturn("Bidder");
        when(mockBidder.getTemporaryMoney()).thenReturn(50.0); // Insufficient funds
        when(mockArticle.getInitialPrice()).thenReturn(100.0);

        auction.registerUser(mockBidder);
        auction.startAuction();

        assertFalse(auction.placeBid(mockBidder, 150.0), "Bid should fail due to insufficient funds");
        assertTrue(auction.getBids().isEmpty(), "No bids should be added");
    }

    @Test
    void testFinalizeAuction_Success() throws AuctionException {
        when(mockBidder.getId()).thenReturn(2L);
        when(mockBidder.getUsername()).thenReturn("Bidder");
        when(mockBidder.getTemporaryMoney()).thenReturn(500.0);
        when(mockBidder.getRealMoney()).thenReturn(500.0);
        when(mockArticle.getInitialPrice()).thenReturn(100.0);

        auction.registerUser(mockBidder);
        auction.startAuction();
        auction.placeBid(mockBidder, 150.0);

        auction.finalizeAuction();

        assertEquals(Auction.AuctionStatus.FINISHED, auction.getStatus(), "Auction should be finished");
        assertEquals(mockBidder, auction.getWinner(), "Bidder should be the winner");
    }

    @Test
    void testFinalizeAuction_Failure() {
        assertThrows(java.lang.IllegalStateException.class, () -> auction.finalizeAuction(), "Finalizing a non-active auction should throw an exception");
    }
    @Test
    void testGetCurrentLeaderNoLeader() {
        // Crear usuarios de ejemplo
        User user1 = new User("user1", "password", "user1@example.com");

        // Crear subasta y simular estado sin leaderboard
        Auction auction2 = new Auction();
        auction2.setRegisteredUsers(Set.of(user1));

        // Verificar que no hay líder
        Optional<User> leader = auction2.getCurrentLeader();
        assertFalse(leader.isPresent());
    }
    @Test
    void testGetRemainingTimeFinished() {
        Auction auction3 = new Auction();
        auction3.setStatus(Auction.AuctionStatus.FINISHED);

        // Tiempo restante debe ser 0
        assertEquals(Duration.ZERO, auction3.getRemainingTime());
    }

    @Test
    void testGetRemainingTimeScheduled() {
        Auction auction4 = new Auction();
        auction4.setStatus(Auction.AuctionStatus.SCHEDULED);
        auction4.setDuration(Duration.ofHours(2));  // Duración de la subasta programada

        // Debería devolver la duración restante de la subasta
        assertEquals(Duration.ofHours(2), auction4.getRemainingTime());
    }

    @Test
    void testGetRemainingTimeActive() {
        // Caso en que la subasta está activa
        Auction auctionTime = new Auction();
        auctionTime.setStatus(Auction.AuctionStatus.ACTIVE);
        auctionTime.setStartTime(LocalDateTime.now().minusMinutes(30));  // Subasta que comenzó hace 30 minutos
        auctionTime.setDuration(Duration.ofHours(2));  // Duración de la subasta

        // Tiempo restante de la subasta
        Duration expectedRemainingTime = Duration.ofMinutes(90);  // 2 horas - 30 minutos
        assertEquals(expectedRemainingTime, auctionTime.getRemainingTime());
    }
    @Test
    void testPlaceBid_HigherBidThanCurrent() {
        // Crear usuario y creador de la subasta
        User user = new User("user1", "password", "user1@example.com");
        User previousHighestUser = new User("user2", "password", "user2@example.com");

        // Crear subasta y establecer el líder de la puja
        Auction auctionhigh = new Auction();
        auctionhigh.setStatus(Auction.AuctionStatus.ACTIVE);
        auctionhigh.setRegisteredUsers(Set.of(user, previousHighestUser));
        auctionhigh.setArticle(new Article());  // Precio inicial de 100
        auctionhigh.setCreator(previousHighestUser);

        // Crear una puja actual
        Bid previousHighestBid = new Bid();
        previousHighestBid.setUser(previousHighestUser);
        previousHighestBid.setAmount(200.0);  // Puja más alta actual
        auctionhigh.getBids().add(previousHighestBid);

        // Establecer dinero disponible para los usuarios
        user.setTemporaryMoney(300.0);  // Dinero disponible para el usuario
        previousHighestUser.setTemporaryMoney(500.0); // Dinero del usuario anterior

        // Llamar a la acción de hacer una puja más alta
        boolean result = auctionhigh.placeBid(user, 250.0); // Hacer una puja de 250

        // Verificar que la puja fue exitosa
        assertTrue(result);

        // Verificar que el dinero del creador (anterior ganador) se actualizó
        assertEquals(750.0, previousHighestUser.getTemporaryMoney(), "El dinero del creador debe actualizarse correctamente.");

        // Verificar que el dinero del usuario que hizo la puja se ha descontado
        assertEquals(50.0, user.getTemporaryMoney(), "El dinero del usuario debe descontarse correctamente.");

        // Verificar que el dinero del anterior ganador (user2) ha sido restaurado con su puja
        assertEquals(750.0, previousHighestUser.getTemporaryMoney(), "El dinero del usuario anterior debe restaurarse.");
    }

    @Test
    void testPlaceBid_LowerBidThanCurrent() {
        // Crear usuario y creador de la subasta
        User user = new User("user1", "password", "user1@example.com");
        User previousHighestUser = new User("user2", "password", "user2@example.com");

        // Crear subasta y establecer el líder de la puja
        Auction auctionlower = new Auction();
        auctionlower.setStatus(Auction.AuctionStatus.ACTIVE);
        auctionlower.setRegisteredUsers(Set.of(user, previousHighestUser));
        auctionlower.setArticle(new Article());  // Precio inicial de 100
        auctionlower.setCreator(previousHighestUser);

        // Crear una puja actual
        Bid previousHighestBid = new Bid();
        previousHighestBid.setUser(previousHighestUser);
        previousHighestBid.setAmount(200.0);  // Puja más alta actual
        auctionlower.getBids().add(previousHighestBid);

        // Establecer dinero disponible para los usuarios
        user.setTemporaryMoney(300.0);  // Dinero disponible para el usuario
        previousHighestUser.setTemporaryMoney(500.0); // Dinero del usuario anterior

        // Intentar hacer una puja menor a la actual
        boolean result = auctionlower.placeBid(user, 150.0); // Hacer una puja de 150, menor a la actual

        // Verificar que la puja no fue exitosa
        assertFalse(result);

        // Verificar que el dinero del creador (anterior ganador) no cambió
        assertEquals(500.0, previousHighestUser.getTemporaryMoney(), "El dinero del creador no debe cambiar.");

        // Verificar que el dinero del usuario que intentó hacer la puja no se ha descontado
        assertEquals(300.0, user.getTemporaryMoney(), "El dinero del usuario no debe descontarse.");
    }
}