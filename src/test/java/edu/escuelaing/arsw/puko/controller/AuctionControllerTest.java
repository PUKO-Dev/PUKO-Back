package edu.escuelaing.arsw.puko.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.escuelaing.arsw.puko.dto.*;
import edu.escuelaing.arsw.puko.exception.AuctionException;
import edu.escuelaing.arsw.puko.exception.UserNotFoundException;
import edu.escuelaing.arsw.puko.model.Article;
import edu.escuelaing.arsw.puko.model.Auction;
import edu.escuelaing.arsw.puko.model.User;
import edu.escuelaing.arsw.puko.service.ArticleService;
import edu.escuelaing.arsw.puko.service.AuctionService;
import edu.escuelaing.arsw.puko.service.UserService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuctionController.class)
class AuctionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuctionService auctionService;

    @MockBean
    private UserService userService;

    @MockBean
    private ArticleService articleService;

    private User mockUser;
    private Article mockArticle;
    private Auction mockAuction;

    @BeforeEach
    public void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");

        mockArticle = new Article();
        mockArticle.setId(1L);
        mockArticle.setUser(mockUser);

        mockAuction = new Auction();
        mockAuction.setId(1L);
        mockAuction.setCreator(mockUser);
        mockAuction.setArticle(mockArticle);
        mockAuction.setStartTime(LocalDateTime.now().plusMinutes(10));
        mockAuction.setDuration(Duration.ofMinutes(30));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreateAuction_Success() throws Exception {
        AuctionCreateDTO auctionCreateDTO = new AuctionCreateDTO();
        auctionCreateDTO.setArticleId(1L);
        auctionCreateDTO.setStartTime(mockAuction.getStartTime());
        auctionCreateDTO.setDuration(mockAuction.getDuration());

        when(userService.findByUsername("testuser")).thenReturn(mockUser);
        when(articleService.findById(1L)).thenReturn(mockArticle);
        when(auctionService.save(Mockito.any(Auction.class))).thenReturn(mockAuction);

        mockMvc.perform(post("/api/auctions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content("""
                                {
                                  "articleId": 1,
                                  "startTime": "%s",
                                  "duration": "PT30M"
                                }
                                """.formatted(mockAuction.getStartTime().toString())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.creatorId").value(mockUser.getId()))
                .andExpect(jsonPath("$.articleId").value(mockArticle.getId()));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testRegisterForAuction_Success() throws Exception {
        when(userService.findByUsername("testuser")).thenReturn(mockUser);
        when(auctionService.registerUserForAuction(1L, mockUser)).thenReturn(true);

        mockMvc.perform(post("/api/auctions/1/register").with(csrf()))

                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetTopBids_Success() throws Exception {
        when(auctionService.getTopBids(1L)).thenReturn(List.of(Map.entry("testuser", 100.0)));
        when(userService.findByUsername("testuser")).thenReturn(mockUser);

        mockMvc.perform(get("/api/auctions/1/top-bids").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[0].amount").value(100.0));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetRemainingTime_Success() throws Exception {
        when(auctionService.getRemainingTime(1L)).thenReturn(Duration.ofMinutes(20));

        MvcResult result = mockMvc.perform(get("/api/auctions/1/remaining-time").with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertEquals("PT20M", responseBody.replace("\"", ""));
    }
    // Prueba: startAuction - Éxito
    @Test
    @WithMockUser(username = "testuser")
    void testStartAuction_Success() throws Exception {
        User mockUserInStartAuction = new User(1L, "testuser", "testuserpass");
        Auction mockAuctionInStartAuction = new Auction(1L, mockUserInStartAuction);
        when(userService.findByUsername("testuser")).thenReturn(mockUserInStartAuction);
        when(auctionService.findById(1L)).thenReturn(Optional.of(mockAuctionInStartAuction));
        when(auctionService.startAuction(1L)).thenReturn(true);

        mockMvc.perform(post("/api/auctions/1/start").with(csrf()))
                .andExpect(status().isOk());
    }

    // Prueba: startAuction - Usuario no encontrado
    @Test
    @WithMockUser(username = "unknownuser")
    void testStartAuction_UserNotFound() throws Exception {
        when(userService.findByUsername("unknownuser")).thenReturn(null);

        mockMvc.perform(post("/api/auctions/1/start").with(csrf()))
                .andExpect(status().isNotFound());
    }

    // Prueba: finalizeAuction - Éxito
    @Test
    @WithMockUser(username = "testuser")
    void testFinalizeAuction_Success() throws Exception {
        User mockUserInFinalice = new User(1L, "testuser", "testuserpass");
        Auction mockAuctionInFinalice = new Auction(1L, mockUserInFinalice);
        when(userService.findByUsername("testuser")).thenReturn(mockUserInFinalice);
        when(auctionService.findById(1L)).thenReturn(Optional.of(mockAuctionInFinalice));

        mockMvc.perform(post("/api/auctions/1/finalize").with(csrf()))
                .andExpect(status().isOk());
    }

    // Prueba: getAvailableAuctions - Éxito
    @Test
    @WithMockUser
    void testGetAvailableAuctions_Success() throws Exception {
        Duration mockDuration = Duration.ofHours(2);

        Auction mockAuctionInAvaliable = new Auction(1L, new User(1L, "testuser", "testuserpass"), new Article(), mockDuration, LocalDateTime.now());
        mockAuctionInAvaliable.setStatus(Auction.AuctionStatus.ACTIVE);
        when(auctionService.findAvailableAuctions()).thenReturn(List.of(mockAuctionInAvaliable));

        mockMvc.perform(get("/api/auctions/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    // Prueba: getRegisteredUsers - Éxito
    @Test
    @WithMockUser
    void testGetRegisteredUsers_Success() throws Exception {
        User user1 = new User(1L, "user1", "testuserpass");
        User user2 = new User(2L, "user2", "testuserpass2");

        when(auctionService.getRegisteredUsers(1L)).thenReturn(Set.of(user1, user2));

        mockMvc.perform(get("/api/auctions/1/registered-users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // Prueba: getWinner - Éxito
    @Test
    @WithMockUser
    void testGetWinner_Success() throws Exception {
        User winner = new User(1L, "winner", "testuserpass");
        when(auctionService.getWinner(1L)).thenReturn(winner);

        mockMvc.perform(get("/api/auctions/1/winner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("winner"));
    }

    // Prueba: getWinner - Subasta sin ganador
    @Test
    @WithMockUser
    void testGetWinner_NoWinner() throws Exception {
        when(auctionService.getWinner(1L)).thenReturn(null);

        MvcResult result = mockMvc.perform(get("/api/auctions/1/winner"))
                .andExpect(status().isBadRequest())
                .andReturn();
        String responseBody = result.getResponse().getContentAsString();
        assertEquals("No hay ganador para la subasta", responseBody);
    }
    @Test
    @WithMockUser(username = "testuser")
    void testGetUserAuctions_Success() throws Exception {
        User mockUserGetAuctionsSuccess = new User(1L, "testuser", "password");
        Auction mockAuctionGetAuctionsSuccess = new Auction(1L, mockUserGetAuctionsSuccess, new Article(), Duration.ofHours(2), LocalDateTime.now());
        mockAuctionGetAuctionsSuccess.setStatus(Auction.AuctionStatus.ACTIVE);
        when(userService.findByUsername("testuser")).thenReturn(mockUserGetAuctionsSuccess);
        when(auctionService.findByCreator(mockUserGetAuctionsSuccess)).thenReturn(List.of(mockAuctionGetAuctionsSuccess));

        mockMvc.perform(get("/api/auctions/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }
    @Test
    @WithMockUser(username = "testuser")
    void testGetRegisteredAuctions_Success() throws Exception {
        User mockUserRegisteredAuction = new User(1L, "testuser", "password");
        Auction mockAuctionRegisteredAuction = new Auction(2L, mockUserRegisteredAuction, new Article(), Duration.ofHours(3), LocalDateTime.now());
        mockAuctionRegisteredAuction.setStatus(Auction.AuctionStatus.ACTIVE);
        when(userService.findByUsername("testuser")).thenReturn(mockUserRegisteredAuction);
        when(auctionService.findByRegisteredUser(mockUserRegisteredAuction)).thenReturn(List.of(mockAuctionRegisteredAuction));

        mockMvc.perform(get("/api/auctions/registered"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }
    @Test
    @WithMockUser
    void testGetAuctionById_Success() throws Exception {
        Auction mockAuctionGetById = new Auction(3L, new User(), new Article(), Duration.ofHours(1), LocalDateTime.now());
        mockAuctionGetById.setStatus(Auction.AuctionStatus.ACTIVE);
        when(auctionService.findById(3L)).thenReturn(Optional.of(mockAuctionGetById));

        mockMvc.perform(get("/api/auctions/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser
    void testGetAuctionById_NotFound() throws Exception {
        when(auctionService.findById(4L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/auctions/4"))
                .andExpect(status().isNotFound());
    }
    @Test
    @WithMockUser(username = "nonexistentuser")
    void testCreateAuction_UserNotFoundException() throws Exception {
        when(userService.findByUsername("nonexistentuser")).thenReturn(null);

        AuctionCreateDTO auctionDTO = new AuctionCreateDTO();

        mockMvc.perform(post("/api/auctions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(auctionDTO)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException));
    }
    @Test
    @WithMockUser(username = "testuser")
    void testCreateAuction_AuctionException_InvalidArticleOwnership() throws Exception {
        User creator = new User(1L, "testuser", "password");
        Article article = new Article( "Test Article", new User(2L, "anotheruser", "password"), 52);

        when(userService.findByUsername("testuser")).thenReturn(creator);
        when(articleService.findById(1L)).thenReturn(article);

        AuctionCreateDTO auctionDTO = new AuctionCreateDTO(1L);

        mockMvc.perform(post("/api/auctions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(new ObjectMapper().writeValueAsString(auctionDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof AuctionException))
                .andExpect(result -> assertEquals("El artículo no pertenece al usuario", result.getResolvedException().getMessage()));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testRegisterForAuction_AuctionException() throws Exception {
        User user = new User(1L, "testuser", "password");

        when(userService.findByUsername("testuser")).thenReturn(user);
        when(auctionService.registerUserForAuction(1L, user)).thenReturn(false);

        mockMvc.perform(post("/api/auctions/1/register").with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof AuctionException))
                .andExpect(result -> assertEquals("No se pudo registrar al usuario en la subasta", result.getResolvedException().getMessage()));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetActiveAuctions_Success() throws Exception {
        // Crear una subasta de ejemplo
        User creator = new User(1L, "testuser", "password");
        Article article = new Article("Test Article", creator, 52);
        Auction activeAuction = new Auction(1L, creator, article, Duration.ofHours(2), LocalDateTime.now().plusMinutes(30)); // Subasta activa

        // Mockear la respuesta del servicio
        when(auctionService.findActiveAuctions()).thenReturn(List.of(activeAuction));

        // Realizar la solicitud GET y validar la respuesta
        mockMvc.perform(get("/api/auctions/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())) // Usar csrf si es necesario en el proyecto
                .andExpect(status().isOk())  // Comprobar que el estado de la respuesta es 200 OK
                .andExpect(jsonPath("$[0].id").value(1))  // Comprobar que el primer elemento tiene id 1
                .andExpect(jsonPath("$[0].duration").value(7200000))  // Comprobar que la duración es 2 horas
                .andExpect(jsonPath("$[0].startTime").exists())  // Comprobar que la fecha de inicio está presente
                .andExpect(jsonPath("$[0].startTime").value(Matchers.notNullValue()));  // Comprobar que la fecha de inicio no es nula
    }
}
