package edu.escuelaing.arsw.puko.model;

import edu.escuelaing.arsw.puko.exception.AuctionException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Auction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Column(nullable = false)
    private Duration duration;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @ManyToMany
    @JoinTable(
            name = "auction_registered_users",
            joinColumns = @JoinColumn(name = "auction_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> registeredUsers = Collections.synchronizedSet(new HashSet<>());

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private volatile AuctionStatus status = AuctionStatus.SCHEDULED;

    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bid> bids = Collections.synchronizedList(new ArrayList<>());

    @Transient
    private final ConcurrentHashMap<String, Double> bidRanking = new ConcurrentHashMap<>();

    @Transient
    private final Object bidLock = new Object(); // objeto específico para sincronización de pujas

    public enum AuctionStatus {
        SCHEDULED,
        ACTIVE,
        FINISHED
    }

    public Auction(User creator, Article article, Duration duration, LocalDateTime startTime) {
        if (article.isInAuction()) {
            throw new AuctionException("El artículo ya está en una subasta");
        }
        this.creator = creator;
        this.article = article;
        this.duration = duration;
        this.startTime = startTime;
        article.setInAuction(true);
    }

    public boolean startAuction() {
        if (status != AuctionStatus.SCHEDULED) {
            return false;
        }

        if (LocalDateTime.now().isBefore(startTime)) {
            startTime = LocalDateTime.now();
        }

        status = AuctionStatus.ACTIVE;
        return true;
    }

    public boolean registerUser(User user) {
        if (user.getId().equals(creator.getId())) {
            return false;
        }

        synchronized(registeredUsers) {
            if (registeredUsers.add(user)) {
                bidRanking.put(user.getUsername(), 0.0);
                return true;
            }
            return false;
        }
    }

    public boolean placeBid(User user, double amount) {
        if (status != AuctionStatus.ACTIVE || !registeredUsers.contains(user) || amount < article.getInitialPrice()) {
            return false;
        }
        synchronized (bidLock) {
            // Verificación inicial de condiciones de subasta
            if (status != AuctionStatus.ACTIVE || !registeredUsers.contains(user) || amount < article.getInitialPrice()) {
                return false;
            }
            Optional<Bid> highestBid = bids.stream().max(Comparator.comparing(Bid::getAmount));

            // Verificar si el usuario tiene fondos suficientes
            if (amount > user.getTemporaryMoney()) {
                return false;
            }
            if(highestBid.isEmpty()){
                creator.setTemporaryMoney(creator.getTemporaryMoney() + amount);
            }
            // Si la nueva puja es mayor a la actual más alta
            if (highestBid.isPresent() && amount > highestBid.get().getAmount()) {
                creator.setTemporaryMoney(creator.getTemporaryMoney() - highestBid.get().getAmount());
                creator.setTemporaryMoney(creator.getTemporaryMoney() + amount);
                // Reintegrar el dinero de la puja superada al usuario anterior
                User previousHighestUser = highestBid.get().getUser();
                previousHighestUser.setTemporaryMoney(previousHighestUser.getTemporaryMoney() + highestBid.get().getAmount());
            } else if (highestBid.isPresent() && amount <= highestBid.get().getAmount()) {
                // Si la puja no es mayor, no procede
                return false;
            }



            // Registrar la nueva puja y descontar el monto del dinero temporal del usuario
            user.setTemporaryMoney(user.getTemporaryMoney() - amount);
            Bid newBid = new Bid();
            newBid.setAuction(this);
            newBid.setUser(user);
            newBid.setAmount(amount);
            newBid.setBidTime(LocalDateTime.now());
            bids.add(newBid); // Agregar la nueva puja más alta

            bidRanking.put(user.getUsername(), amount);
            return true;
        }
    }





    private List<Map.Entry<String, Double>> getLeaderboard() {
        return bidRanking.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed()) // Asegúrate de que se ordena correctamente
                .toList();
    }


    public List<Map.Entry<String, Double>> getTopBids() {
        // Asegurarse de que el ranking esté actualizado antes de devolverlo
        initializeBidRanking();

        return getLeaderboard().stream()
                .limit(10) // Limitar a los 10 primeros
                .toList();
    }


    // Método para obtener el usuario que está en primer lugar
    public Optional<User> getCurrentLeader() {
        return getLeaderboard().stream()
                .findFirst()
                .map(entry -> registeredUsers.stream()
                        .filter(user -> user.getUsername().equals(entry.getKey())) // Comparar por username
                        .findFirst()
                        .orElse(null));
    }


    public Duration getRemainingTime() {
        if (status == AuctionStatus.FINISHED) {
            return Duration.ZERO;
        }

        if (status == AuctionStatus.SCHEDULED) {
            return duration;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = startTime.plus(duration);

        if (now.isAfter(endTime)) {
            return Duration.ZERO;
        }

        return Duration.between(now, endTime);
    }

    @SneakyThrows
    public void finalizeAuction() throws AuctionException {
        // Verificación inicial del estado
        if (status != AuctionStatus.ACTIVE) {
            throw new IllegalStateException("No se puede finalizar una subasta que no está activa");
        }

        synchronized(bidLock) {
            // Double-check del estado dentro del bloque sincronizado
            if (status != AuctionStatus.ACTIVE) {
                throw new IllegalStateException("No se puede finalizar una subasta que no está activa");
            }

            try {


                status = AuctionStatus.FINISHED;
                article.setInAuction(false);

                // Identificar el ganador (la puja más alta)
                Optional<Bid> winningBid = bids.stream()
                        .max(Comparator.comparing(Bid::getAmount));

                // Procesar la puja ganadora si existe
                winningBid.ifPresent(this::processWinningBid);
            } catch (Exception e) {
                // En caso de error, revertir el estado
                status = AuctionStatus.ACTIVE;
                throw new AuctionException("Error al finalizar la subasta");
            }
        }
    }

    public User getWinner() {
        if (status != AuctionStatus.FINISHED) {
            return null;
        }
        return bids.stream()
                .max(Comparator.comparing(Bid::getAmount))
                .map(Bid::getUser)
                .orElse(null);
    }

    private void processWinningBid(Bid winningBid){
        User winner = winningBid.getUser();
        double winningAmount = winningBid.getAmount();

        // Validar que el ganador tiene suficiente dinero real
        if (winner.getRealMoney() < winningAmount) {
            throw new AuctionException("El ganador no tiene suficiente dinero real");
        }

        // Realizar las transacciones
        winner.setRealMoney(winner.getRealMoney() - winningAmount);
        creator.setRealMoney(creator.getRealMoney() + winningAmount);

        // Actualizar el dinero temporal del creador
        // Nota: Considera si esto es necesario, ya que el creador ya recibió el dinero real
        creator.setTemporaryMoney(creator.getTemporaryMoney() + winningAmount);
    }



    // Método para inicializar el ranking desde las pujas existentes
    public void initializeBidRanking() {
        synchronized(bidLock) {
            // Limpiar el ranking actual
            bidRanking.clear();

            // Inicializar todos los usuarios registrados con 0
            registeredUsers.forEach(user -> bidRanking.put(user.getUsername(), 0.0)); // Usar username

            // Actualizar el ranking con la puja más alta de cada usuario
            bids.stream()
                    .collect(Collectors.groupingBy(
                            bid -> bid.getUser().getUsername(), // Cambiar a username
                            Collectors.maxBy(Comparator.comparing(Bid::getAmount))
                    ))
                    .forEach((username, maxBid) ->
                            maxBid.ifPresent(bid -> bidRanking.put(username, bid.getAmount()))
                    );
        }
    }


    @PostLoad
    private void onLoad() {
        initializeBidRanking();
    }

    @Override
    public String toString() {
        return "Auction{" +
                "id=" + id +
                ", creator=" + creator +
                ", article=" + article +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", status=" + status +
                '}';
    }


}
