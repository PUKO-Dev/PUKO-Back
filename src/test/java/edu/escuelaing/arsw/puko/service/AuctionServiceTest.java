package edu.escuelaing.arsw.puko.service;

import edu.escuelaing.arsw.puko.exception.AuctionException;
import edu.escuelaing.arsw.puko.exception.AuctionNotFoundException;
import edu.escuelaing.arsw.puko.model.Article;
import edu.escuelaing.arsw.puko.model.Auction;
import edu.escuelaing.arsw.puko.model.User;
import edu.escuelaing.arsw.puko.repository.AuctionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;

class AuctionServiceTest {

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private AuctionEventPublisher auctionEventPublisher;

    @InjectMocks
    private AuctionService auctionService;

    private Auction auction;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Crear datos de prueba
        user = new User(1L, "testuser", "password");
        auction = new Auction(1L, user, new Article(), Duration.ofHours(2), LocalDateTime.now().plusDays(1));
    }

    @Test
    void testSaveAuction() {
        // Mockear el comportamiento de auctionRepository.save()
        when(auctionRepository.save(any(Auction.class))).thenReturn(auction);

        // Llamar al servicio
        Auction savedAuction = auctionService.save(auction);

        // Verificar que el método save fue llamado
        verify(auctionRepository, times(1)).save(any(Auction.class));
        assertNotNull(savedAuction);
        assertEquals(auction.getId(), savedAuction.getId());
    }

    @Test
    void testRegisterUserForAuction() {


        // Crear un mock de Auction
        Auction auctionRegisteredUser = mock(Auction.class);

        // Crear un mock de User
        User userRegistered = mock(User.class);


        // Configurar el comportamiento esperado para la búsqueda de subasta
        when(auctionRepository.findById(auctionRegisteredUser.getId())).thenReturn(Optional.of(auctionRegisteredUser));

        // Simular que el método registerUser() devuelve true
        when(auctionRegisteredUser.registerUser(userRegistered)).thenReturn(true);

        // Ejecutar el método que se va a probar
        boolean result = auctionService.registerUserForAuction(auctionRegisteredUser.getId(), userRegistered);

        // Verificar que el resultado sea el esperado
        assertTrue(result);

        // Verificar que se haya guardado la subasta y se haya publicado el evento
        verify(auctionRepository, times(1)).save(auctionRegisteredUser);
        verify(auctionEventPublisher, times(1)).publishAuctionEvent(auctionRegisteredUser.getId(), "USER_REGISTERED", auction.getTopBids());
    }

    @Test
    void testPlaceBid() {

        // Crear un mock de Auction
        Auction auctionPlaceBid = mock(Auction.class);

        // Crear un mock de User
        User userPlaceBid = mock(User.class);

        // Configurar el comportamiento esperado para la subasta
        when(auctionPlaceBid.getId()).thenReturn(1L);  // Mock para el ID de la subasta
        when(auctionPlaceBid.getStatus()).thenReturn(Auction.AuctionStatus.ACTIVE);  // La subasta debe estar activa
        when(auctionPlaceBid.placeBid(userPlaceBid, 100)).thenReturn(true);  // La puja es exitosa

        // Configurar el comportamiento esperado para la búsqueda de la subasta
        when(auctionRepository.findAuctionForUpdate(auctionPlaceBid.getId())).thenReturn(Optional.of(auctionPlaceBid));

        // Ejecutar el método que se va a probar
        boolean result = auctionService.placeBid(auctionPlaceBid.getId(), userPlaceBid, 100);

        // Verificar que el resultado sea el esperado
        assertTrue(result);

        // Verificar que se haya guardado la subasta y se haya publicado el evento
        verify(auctionRepository, times(2)).save(auctionPlaceBid);

    }

    @Test
    void testFindActiveAuctions() {
        // Crear un objeto Auction para simularlo
        Auction auctionFindActive = mock(Auction.class);
        auctionFindActive.setId(1L);
        auctionFindActive.setStatus(Auction.AuctionStatus.ACTIVE);

        // Mockear la búsqueda de subastas activas
        when(auctionRepository.findByStatus(Auction.AuctionStatus.ACTIVE)).thenReturn(List.of(auctionFindActive));

        // Llamar al servicio
        List<Auction> activeAuctions = auctionService.findActiveAuctions();

        assertFalse(activeAuctions.isEmpty());
        assertEquals(1, activeAuctions.size());
        assertEquals(auctionFindActive.getId(), activeAuctions.get(0).getId());
    }

    @Test
    void testStartAuction() {
        // Mockear la subasta y la inicialización de ranking
        Auction auctionStartAuction = mock(Auction.class);
        when(auctionRepository.findById(auctionStartAuction.getId())).thenReturn(Optional.of(auctionStartAuction));
        when(auctionStartAuction.startAuction()).thenReturn(true);

        boolean started = auctionService.startAuction(auctionStartAuction.getId());

        assertTrue(started);
        verify(auctionRepository, times(1)).save(auctionStartAuction);
        verify(auctionEventPublisher, times(1)).publishAuctionEvent(auctionStartAuction.getId(), "AUCTION_STARTED", null);
    }

    @Test
    void testFinalizeAuction() {
        // Mockear la subasta
        Auction auctionFinalizeAuction = mock(Auction.class);
        when(auctionRepository.findById(auctionFinalizeAuction.getId())).thenReturn(Optional.of(auctionFinalizeAuction));
        doNothing().when(auctionFinalizeAuction).finalizeAuction();

        // Llamar al servicio
        assertDoesNotThrow(() -> auctionService.finalizeAuction(auctionFinalizeAuction.getId()));

        verify(auctionRepository, times(1)).save(auctionFinalizeAuction);
        verify(auctionEventPublisher, times(1)).publishAuctionEvent(auctionFinalizeAuction.getId(), "AUCTION_FINALIZED", auction.getWinner());
    }

    @Test
    void testFindById() {
        Auction auctionById = mock(Auction.class);
        // Mockear la búsqueda de una subasta por ID
        when(auctionRepository.findById(auctionById.getId())).thenReturn(Optional.of(auctionById));

        Optional<Auction> foundAuction = auctionService.findById(auctionById.getId());

        assertTrue(foundAuction.isPresent());
        assertEquals(auctionById.getId(), foundAuction.get().getId());
    }
    @Test
    void testUpdateAuctionStatuses() throws AuctionException {
        Auction auctionUpdate = mock(Auction.class);
        User userUpdate = mock(User.class);
        // Preparar mocks
        LocalDateTime now = LocalDateTime.now();
        when(auctionRepository.findByStatus(Auction.AuctionStatus.SCHEDULED)).thenReturn(List.of(auctionUpdate));
        when(auctionRepository.findByStatus(Auction.AuctionStatus.ACTIVE)).thenReturn(List.of(auctionUpdate));
        when(auctionUpdate.getStartTime()).thenReturn(now.minusMinutes(1));  // Simulando que la subasta ha comenzado
        when(auctionUpdate.getDuration()).thenReturn(Duration.ofMinutes(5));
        when(auctionUpdate.getWinner()).thenReturn(userUpdate);

        // Simular métodos que se llaman durante la ejecución
        auctionService.updateAuctionStatuses();  // Llamar al método programado

        // Verificar interacciones
        verify(auctionRepository, times(1)).save(auctionUpdate);  // Se espera que se guarde la subasta una vez al cambiar el estado y otra al finalizar
        verify(auctionEventPublisher, times(1)).publishAuctionEvent(anyLong(), anyString(), any());
        verify(auctionEventPublisher, times(1)).publishAuctionAvailableEvent(anyString(), anyLong());
    }
    @Test
    void testGetTopBids() {
        // Simular el comportamiento del repositorio
        Auction auctionTopBids = mock(Auction.class);

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auctionTopBids));
        when(auctionTopBids.getTopBids()).thenReturn(List.of(new AbstractMap.SimpleEntry<>("User1", 100.0)));

        // Llamar al método que se va a probar
        List<Map.Entry<String, Double>> topBids = auctionService.getTopBids(1L);

        // Verificar que el resultado es correcto
        assertNotNull(topBids);
        assertEquals(1, topBids.size());
        assertEquals("User1", topBids.get(0).getKey());
        assertEquals(100.0, topBids.get(0).getValue());
    }
    @Test
    void testGetRemainingTime() {
        Auction auctionRemainingTime = mock(Auction.class);

        // Simular el comportamiento del repositorio
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auctionRemainingTime));
        when(auctionRemainingTime.getRemainingTime()).thenReturn(Duration.ofMinutes(10));

        // Llamar al método que se va a probar
        Duration remainingTime = auctionService.getRemainingTime(1L);

        // Verificar que el resultado es correcto
        assertNotNull(remainingTime);
        assertEquals(Duration.ofMinutes(10), remainingTime);
    }
    @Test
    void testFindByCreator() {
        // Simular comportamiento del repositorio
        Auction auctionFindCreator = mock(Auction.class);
        User userCreator = mock(User.class);
        when(auctionRepository.findByCreator(userCreator)).thenReturn(List.of(auctionFindCreator));
        when(auctionFindCreator.getTopBids()).thenReturn(Collections.emptyList());  // Suponiendo que no hay pujas

        // Llamar al método
        List<Auction> auctions = auctionService.findByCreator(userCreator);

        // Verificar que se haya inicializado el ranking de pujas
        assertFalse(auctions.isEmpty());
        verify(auctionFindCreator, times(1)).initializeBidRanking();
    }

    @Test
    void testFindByRegisteredUser() {
        // Simular comportamiento del repositorio
        Auction auctionRegisteredUser = mock(Auction.class);
        User userRegistered = mock(User.class);
        when(auctionRepository.findByRegisteredUser(userRegistered)).thenReturn(List.of(auctionRegisteredUser));
        when(auctionRegisteredUser.getTopBids()).thenReturn(Collections.emptyList());

        // Llamar al método
        List<Auction> auctions = auctionService.findByRegisteredUser(userRegistered);

        // Verificar que se haya inicializado el ranking de pujas
        assertFalse(auctions.isEmpty());
        verify(auctionRegisteredUser, times(1)).initializeBidRanking();
    }
    @Test
    void testFindAvailableAuctions() {
        Auction auctionAvaliable = mock(Auction.class);

        // Simular que el repositorio devuelve una lista de subastas disponibles
        when(auctionRepository.findAvailableAuctions()).thenReturn(List.of(auctionAvaliable));

        // Llamar al método que se va a probar
        List<Auction> availableAuctions = auctionService.findAvailableAuctions();

        // Verificar que las subastas no estén vacías
        assertFalse(availableAuctions.isEmpty());
        assertEquals(1, availableAuctions.size());

        // Verificar que el ranking de pujas se haya inicializado
        verify(auctionAvaliable, times(1)).initializeBidRanking();
    }
    @Test
    void testGetRegisteredUsers() {
        Auction auctionRegisteredUsers = mock(Auction.class);
        User userRegistered = mock(User.class);
        Long auctionId = 1L;

        // Simular la búsqueda de la subasta
        when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auctionRegisteredUsers));

        // Simular la lista de usuarios registrados
        when(auctionRegisteredUsers.getRegisteredUsers()).thenReturn(Set.of(userRegistered));

        // Llamar al método que se va a probar
        Set<User> registeredUsers = auctionService.getRegisteredUsers(auctionId);

        // Verificar que los usuarios registrados no estén vacíos
        assertFalse(registeredUsers.isEmpty());
        assertEquals(1, registeredUsers.size());
        assertTrue(registeredUsers.contains(userRegistered));

        // Verificar que se inicializó el ranking de pujas
        verify(auctionRegisteredUsers, times(1)).initializeBidRanking();
    }

    @Test
    void testGetRegisteredUsers_AuctionNotFound() {
        Long auctionId = 1L;

        // Simular que no se encuentra la subasta
        when(auctionRepository.findById(auctionId)).thenReturn(Optional.empty());

        // Verificar que se lanza la excepción correspondiente
        assertThrows(AuctionNotFoundException.class, () -> auctionService.getRegisteredUsers(auctionId));
    }
}

