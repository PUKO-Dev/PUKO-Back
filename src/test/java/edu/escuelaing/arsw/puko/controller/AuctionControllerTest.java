package edu.escuelaing.arsw.puko.controller;

import edu.escuelaing.arsw.puko.dto.*;
import edu.escuelaing.arsw.puko.exception.AuctionException;
import edu.escuelaing.arsw.puko.model.Article;
import edu.escuelaing.arsw.puko.model.Auction;
import edu.escuelaing.arsw.puko.model.User;
import edu.escuelaing.arsw.puko.service.ArticleService;
import edu.escuelaing.arsw.puko.service.AuctionService;
import edu.escuelaing.arsw.puko.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuctionControllerTest {

    @InjectMocks
    private AuctionController auctionController;

    @Mock
    private AuctionService auctionService;

    @Mock
    private UserService userService;

    @Mock
    private ArticleService articleService;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(userDetails.getUsername()).thenReturn("testUser");
    }

    @Test
    public void testCreateAuctionSuccess() {
        // Mock de User
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(1L);
        when(mockUser.getUsername()).thenReturn("testUser");

        // Mock de Article
        Article mockArticle = mock(Article.class);
        when(mockArticle.getId()).thenReturn(1L);
        when(mockArticle.getUser()).thenReturn(mockUser);

        // Configurar AuctionCreateDTO
        AuctionCreateDTO auctionDTO = new AuctionCreateDTO();
        auctionDTO.setArticleId(1L);
        auctionDTO.setDuration(Duration.ofHours(2));
        auctionDTO.setStartTime(LocalDateTime.now().plusMinutes(30));

        // Mock de Auction
        Auction mockAuction = mock(Auction.class);
        when(mockAuction.getCreator()).thenReturn(mockUser);
        when(mockAuction.getArticle()).thenReturn(mockArticle);
        when(mockAuction.getDuration()).thenReturn(Duration.ofHours(2));
        when(mockAuction.getStartTime()).thenReturn(LocalDateTime.now().plusMinutes(30));

        // Configurar mocks para servicios
        when(userService.findByUsername("testUser")).thenReturn(mockUser);
        when(articleService.findById(1L)).thenReturn(mockArticle);
        when(auctionService.save(any(Auction.class))).thenReturn(mockAuction);

        // Llamar al método y verificar
        ResponseEntity<AuctionDTO> response = auctionController.createAuction(userDetails, auctionDTO);

        // Verificaciones
        assertEquals(201, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        //assertEquals(mockUser.getUsername(), response.getBody().getCreator().getUsername());
        assertEquals(mockArticle.getId(), response.getBody().getArticleId());
    }
    @Test
    public void testGetAuctionSuccess() {
        // Mock de User
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(1L);
        when(mockUser.getUsername()).thenReturn("testUser");

        // Mock de Article
        Article mockArticle = mock(Article.class);
        when(mockArticle.getId()).thenReturn(1L);
        when(mockArticle.getUser()).thenReturn(mockUser);

        // Mock de Auction
        Auction mockAuction = mock(Auction.class);
        when(mockAuction.getId()).thenReturn(1L);
        when(mockAuction.getStartTime()).thenReturn(LocalDateTime.now());
        when(mockAuction.getCreator()).thenReturn(mockUser);
        when(mockAuction.getArticle()).thenReturn(mockArticle);

        // Configurar mocks
        when(auctionService.findById(1L)).thenReturn(Optional.of(mockAuction));

        // Llamar al método y verificar
        ResponseEntity<AuctionDTO> response = auctionController.getAuction(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(mockAuction.getId(), response.getBody().getId());
    }
    @Test
    public void testRegisterForAuctionSuccess() {
        // Mock de User
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(1L);
        when(mockUser.getUsername()).thenReturn("testUser");

        // Configurar mocks
        when(userService.findByUsername("testUser")).thenReturn(mockUser);
        when(auctionService.registerUserForAuction(1L, mockUser)).thenReturn(true);

        // Llamar al método y verificar
        ResponseEntity<Void> response = auctionController.registerForAuction(userDetails, 1L);

        assertEquals(200, response.getStatusCodeValue());
    }
    @Test
    public void testPlaceBidSuccess() {
        // Mock de User
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(1L);
        when(mockUser.getUsername()).thenReturn("testUser");

        // Configurar mocks
        when(userService.findByUsername("testUser")).thenReturn(mockUser);
        when(auctionService.placeBid(1L, mockUser, 100.0)).thenReturn(true);
        when(auctionService.getTopBids(1L)).thenReturn(List.of(Map.entry("testUser", 100.0)));

        // Datos de prueba
        BidDTO bidDTO = new BidDTO();
        bidDTO.setAmount(100.0);

        // Llamar al método y verificar
        ResponseEntity<BidRankingDTO> response = auctionController.placeBid(userDetails, 1L, bidDTO);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(mockUser.getUsername(), response.getBody().getUsername());
        assertEquals(100.0, response.getBody().getAmount());
    }
    @Test
    public void testGetTopBidsSuccess() {
        // Mock de User
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(1L);
        when(mockUser.getUsername()).thenReturn("testUser");

        // Configurar mocks
        when(auctionService.getTopBids(1L)).thenReturn(List.of(Map.entry("testUser", 200.0)));
        when(userService.findByUsername("testUser")).thenReturn(mockUser);

        // Llamar al método y verificar
        ResponseEntity<List<BidRankingDTO>> response = auctionController.getTopBids(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(200.0, response.getBody().get(0).getAmount());
    }
}