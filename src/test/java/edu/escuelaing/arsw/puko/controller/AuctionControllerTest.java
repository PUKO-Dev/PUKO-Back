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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class AuctionControllerTest {

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
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(userDetails.getUsername()).thenReturn("testUser");
    }

    @Test
    void testCreateAuctionSuccess() {
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
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertEquals(mockArticle.getId(), response.getBody().getArticleId());
    }
    @Test
    void testGetAuctionSuccess() {
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

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertEquals(mockAuction.getId(), response.getBody().getId());
    }
    @Test
    void testRegisterForAuctionSuccess() {
        // Mock de User
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(1L);
        when(mockUser.getUsername()).thenReturn("testUser");

        // Configurar mocks
        when(userService.findByUsername("testUser")).thenReturn(mockUser);
        when(auctionService.registerUserForAuction(1L, mockUser)).thenReturn(true);

        // Llamar al método y verificar
        ResponseEntity<Void> response = auctionController.registerForAuction(userDetails, 1L);

        assertTrue(response.getStatusCode().is2xxSuccessful());
    }
    @Test
    void testGetTopBidsSuccess() {
        // Mock de User
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(1L);
        when(mockUser.getUsername()).thenReturn("testUser");

        // Configurar mocks
        when(auctionService.getTopBids(1L)).thenReturn(List.of(Map.entry("testUser", 200.0)));
        when(userService.findByUsername("testUser")).thenReturn(mockUser);

        // Llamar al método y verificar
        ResponseEntity<List<BidRankingDTO>> response = auctionController.getTopBids(1L);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(200.0, response.getBody().get(0).getAmount());
    }
    @Test
    void testStartAuctionSuccess() {
        Long auctionId = 1L;
        Article mockArticle = mock(Article.class);
        User mockUser = mock(User.class);
        mockUser.setId(1L);
        Auction mockAuction = new Auction(mockUser, mockArticle, null, null);
        mockAuction.setId(auctionId);

        UserDetails userDetails6 = mock(UserDetails.class);
        when(userDetails6.getUsername()).thenReturn("testUser");
        when(userService.findByUsername("testUser")).thenReturn(mockUser);
        when(auctionService.findById(auctionId)).thenReturn(Optional.of(mockAuction));
        when(auctionService.startAuction(auctionId)).thenReturn(true);

        ResponseEntity<Void> response = auctionController.startAuction(auctionId, userDetails6);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        verify(auctionService).startAuction(auctionId);
    }

    @Test
    void testStartAuctionUserNotCreator() {
        Long auctionId = 1L;
        Article mockArticle = mock(Article.class);
        User mockUser = mock(User.class);
        mockUser.setId(1L);
        User anotherUser = mock(User.class);
        anotherUser.setId(2L);
        Auction mockAuction = new Auction(anotherUser, mockArticle, null, null);
        mockAuction.setId(auctionId);
        UserDetails userDetails5 = mock(UserDetails.class);
        when(userDetails5.getUsername()).thenReturn("testUser");
        when(userService.findByUsername("testUser")).thenReturn(mockUser);
        when(auctionService.findById(auctionId)).thenReturn(Optional.of(mockAuction));
        try{
            auctionController.startAuction(auctionId, userDetails5);
        }catch (AuctionException e){
            assertEquals("No se pudo iniciar la subasta", e.getMessage()); // Forbidden
        }
    }

    @Test
    void testFinalizeAuctionSuccess() throws Exception {
        Long auctionId = 1L;
        Article mockArticle = mock(Article.class);
        User mockUser = mock(User.class);
        mockUser.setId(1L);
        Auction mockAuction = new Auction(mockUser, mockArticle, null, null);
        mockAuction.setId(auctionId);

        UserDetails userDetails4 = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("testUser");
        when(userService.findByUsername("testUser")).thenReturn(mockUser);
        when(auctionService.findById(auctionId)).thenReturn(Optional.of(mockAuction));

        ResponseEntity<Void> response = auctionController.finalizeAuction(auctionId, userDetails4);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        verify(auctionService).finalizeAuction(auctionId);
    }

    @Test
    void testGetUserAuctionsSuccess() {
        Article mockArticle = mock(Article.class);
        User mockUser = mock(User.class);
        mockUser.setId(1L);
        Auction mockAuction = new Auction(mockUser, mockArticle, Duration.ofHours(1), LocalDateTime.now());
        mockAuction.setId(1L);

        UserDetails userDetails2 = mock(UserDetails.class);
        when(userDetails2.getUsername()).thenReturn("testUser");
        when(userService.findByUsername("testUser")).thenReturn(mockUser);
        when(auctionService.findByCreator(mockUser)).thenReturn(List.of(mockAuction));

        ResponseEntity<List<AuctionDTO>> response = auctionController.getUserAuctions(userDetails2);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(mockAuction.getId(), response.getBody().get(0).getId());
    }

    @Test
    void testGetRegisteredAuctionsSuccess() {
        Article mockArticle = mock(Article.class);
        User mockUser = mock(User.class);
        mockUser.setId(1L);
        Auction mockAuction = new Auction(mockUser, mockArticle, Duration.ofHours(1), LocalDateTime.now());
        mockAuction.setId(1L);

        UserDetails userDetails3 = mock(UserDetails.class);
        when(userDetails3.getUsername()).thenReturn("testUser");
        when(userService.findByUsername("testUser")).thenReturn(mockUser);
        when(auctionService.findByRegisteredUser(mockUser)).thenReturn(List.of(mockAuction));

        ResponseEntity<List<AuctionDTO>> response = auctionController.getRegisteredAuctions(userDetails3);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(mockAuction.getId(), response.getBody().get(0).getId());
    }
}