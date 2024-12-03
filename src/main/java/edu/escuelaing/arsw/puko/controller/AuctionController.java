package edu.escuelaing.arsw.puko.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.escuelaing.arsw.puko.config.Encryption;
import edu.escuelaing.arsw.puko.dto.*;
import edu.escuelaing.arsw.puko.exception.AuctionException;
import edu.escuelaing.arsw.puko.exception.AuctionNotFoundException;
import edu.escuelaing.arsw.puko.exception.UserNotFoundException;
import edu.escuelaing.arsw.puko.model.Article;
import edu.escuelaing.arsw.puko.model.Auction;
import edu.escuelaing.arsw.puko.model.User;
import edu.escuelaing.arsw.puko.service.ArticleService;
import edu.escuelaing.arsw.puko.service.AuctionService;
import edu.escuelaing.arsw.puko.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    private AuctionService auctionService;

    private UserService userService;

    private ArticleService articleService;

    @Autowired
    public AuctionController(AuctionService auctionService, UserService userService, ArticleService articleService) {
        this.auctionService = auctionService;
        this.userService = userService;
        this.articleService = articleService;
    }

    @PostMapping
    public ResponseEntity<AuctionDTO> createAuction(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AuctionCreateDTO auctionDTO) {
        User creator = userService.findByUsername(userDetails.getUsername());

        if (creator == null) {
            throw new UserNotFoundException(userDetails.getUsername());
        }

        Article article = articleService.findById(auctionDTO.getArticleId()) ;

        // Validar que el artículo pertenece al usuario
        if (!article.getUser().getId().equals(creator.getId())) {
            throw new AuctionException("El artículo no pertenece al usuario");
        }

        if (auctionDTO.getStartTime().isBefore(LocalDateTime.now())){
            throw new AuctionException("La fecha de inicio no puede ser en el pasado");
        }

        Auction auction = new Auction(
                creator,
                article,
                auctionDTO.getDuration(),
                auctionDTO.getStartTime()
        );

        Auction auctionCreated = auctionService.save(auction);

        if (auctionCreated == null) {
            throw new AuctionException("No se pudo crear la subasta");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(AuctionDTO.fromAuction(auctionCreated));
    }

    @PostMapping("/{auctionId}/register")
    public ResponseEntity<Void> registerForAuction(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long auctionId) {
        User user = userService.findByUsername(userDetails.getUsername());

        boolean registered = auctionService.registerUserForAuction(auctionId, user);

        if (!registered) {
            throw new AuctionException("No se pudo registrar al usuario en la subasta");
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{auctionId}/bid")
    public ResponseEntity<BidRankingDTO> placeBid(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long auctionId,
            @Valid @RequestBody Map<String, String> payload) {

        String encryptedData = payload.get("data");
        String decryptedData = Encryption.decrypt(encryptedData);
        if ("Error decrypting data".equals(decryptedData)) {
            throw new AuctionException("Error descifrando los datos de la puja");
        }

        BidDTO bidDTO = parseBidDTO(decryptedData);
        User user = userService.findByUsername(userDetails.getUsername());
        boolean bidPlaced = auctionService.placeBid(auctionId, user, bidDTO.getAmount());

        if (!bidPlaced) {
            throw new AuctionException("No se pudo realizar la puja");
        }

        return ResponseEntity.ok().build();
    }




    @GetMapping("/{auctionId}/top-bids")
    public ResponseEntity<List<BidRankingDTO>> getTopBids(@PathVariable Long auctionId) {
        List<Map.Entry<String, Double>> topBids = auctionService.getTopBids(auctionId);

        if (topBids.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<BidRankingDTO> rankingDTOs = topBids.stream()
                .map(entry -> {
                    User user = userService.findByUsername(entry.getKey());
                    if (user != null) {
                        return new BidRankingDTO(
                                user.getId(),
                                user.getUsername(),
                                entry.getValue()
                        );
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();

        return ResponseEntity.ok(rankingDTOs);
    }

    @GetMapping("/{auctionId}/remaining-time")
    public ResponseEntity<Duration> getRemainingTime(
            @PathVariable Long auctionId) {
        return ResponseEntity.ok(auctionService.getRemainingTime(auctionId));
    }

    @GetMapping("/{auctionId}")
    public ResponseEntity<AuctionDTO> getAuction(@PathVariable Long auctionId) {
        Optional<Auction> auction = auctionService.findById(auctionId);
        if (auction.isPresent()) {
            return ResponseEntity.ok(AuctionDTO.fromAuction(auction.get()));
        }
        throw new AuctionNotFoundException(auctionId);
    }

    @GetMapping("/active")
    public ResponseEntity<List<AuctionDTO>> getActiveAuctions() {
        List<Auction> auctions = auctionService.findActiveAuctions();

        // Convertir la lista de Auction a AuctionDTO
        List<AuctionDTO> auctionDTOs = auctions.stream()
                .map(AuctionDTO::fromAuction)
                .toList();

        return ResponseEntity.ok(auctionDTOs);
    }


    @GetMapping("/user")
    public ResponseEntity<List<AuctionDTO>> getUserAuctions(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());

        // Obtener las subastas del usuario
        List<Auction> userAuctions = auctionService.findByCreator(user);

        // Convertir la lista de Auction a AuctionDTO
        List<AuctionDTO> auctionDTOs = userAuctions.stream()
                .map(AuctionDTO::fromAuction)
                .toList();

        return ResponseEntity.ok(auctionDTOs);
    }


    @GetMapping("/registered")
    public ResponseEntity<List<AuctionDTO>> getRegisteredAuctions(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());

        // Obtener las subastas en las que el usuario está registrado
        List<Auction> registeredAuctions = auctionService.findByRegisteredUser(user);

        // Convertir la lista de Auction a AuctionDTO
        List<AuctionDTO> auctionDTOs = registeredAuctions.stream()
                .map(AuctionDTO::fromAuction)
                .toList();

        return ResponseEntity.ok(auctionDTOs);
    }


    @PostMapping("/{auctionId}/start")
    public ResponseEntity<Void> startAuction(
            @PathVariable Long auctionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        if (user == null) {
            throw new UserNotFoundException("Usuario no encontrado para:" + userDetails.getUsername()); // Usuario no encontrado
        }

        // Verificar si el usuario es el creador de la subasta
        Optional<Auction> auction = auctionService.findById(auctionId);
        if (auction.isEmpty()) {
            throw new AuctionNotFoundException(auctionId); // Subasta no encontrada
        }

        if (!auction.get().getCreator().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Usuario no es el creador
        }

        // Intentar iniciar la subasta
        boolean started = auctionService.startAuction(auctionId);

        if (!started) {
            throw new AuctionException("No se pudo iniciar la subasta");
        }

        return ResponseEntity.ok().build(); // Devolver respuesta según el estado
    }

    @PostMapping("/{auctionId}/finalize")
    public ResponseEntity<Void> finalizeAuction(
            @PathVariable Long auctionId,
            @AuthenticationPrincipal UserDetails userDetails) throws AuctionException {
        auctionService.finalizeAuction(auctionId);
        User user = userService.findByUsername(userDetails.getUsername());

        if (user == null) {
            throw new UserNotFoundException("Usuario no encontrado para:" + userDetails.getUsername()); // Usuario no encontrado
        }

        // Verificar si el usuario es el creador de la subasta
        Optional<Auction> auction = auctionService.findById(auctionId);
        if (auction.isEmpty()) {
            throw new AuctionNotFoundException(auctionId); // Subasta no encontrada
        }

        if (!auction.get().getCreator().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Usuario no es el creador
        }

        return ResponseEntity.ok().build();
    }


    @GetMapping("/available")
    public ResponseEntity<List<AuctionDTO>> getAvailableAuctions() {
        // Obtener las subastas disponibles
        List<Auction> availableAuctions = auctionService.findAvailableAuctions();

        // Convertir la lista de Auction a AuctionDTO
        List<AuctionDTO> auctionDTOs = availableAuctions.stream()
                .map(AuctionDTO::fromAuction)
                .toList();

        return ResponseEntity.ok(auctionDTOs);
    }

    @GetMapping("/{auctionId}/registered-users")
    public ResponseEntity<Set<UserDTO>> getRegisteredUsers(@PathVariable Long auctionId) {
        Set<User> registeredUsers = auctionService.getRegisteredUsers(auctionId);

        Set<UserDTO> userDTOs = registeredUsers.stream()
                .map(UserDTO::fromUser)
                .collect(Collectors.toSet());

        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping("/{auctionId}/winner")
    public ResponseEntity<UserDTO> getWinner(@PathVariable Long auctionId) {
        User winner = auctionService.getWinner(auctionId);

        if (winner == null) {
            throw new AuctionException("No hay ganador para la subasta");
        }

        return ResponseEntity.ok(UserDTO.fromUser(winner));
    }
    private BidDTO parseBidDTO(String decryptedData) {
        // Implementar la lógica para convertir el JSON descifrado en un objeto BidDTO
        try {
            return new ObjectMapper().readValue(decryptedData, BidDTO.class);
        } catch (Exception e) {
            throw new AuctionException("Error al procesar los datos de la puja");
        }
    }

}