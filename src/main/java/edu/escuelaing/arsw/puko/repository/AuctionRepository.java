package edu.escuelaing.arsw.puko.repository;

import edu.escuelaing.arsw.puko.model.Auction;
import edu.escuelaing.arsw.puko.model.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {

    // Buscar por estado
    List<Auction> findByStatus(Auction.AuctionStatus status);

    @Query("SELECT a FROM Auction a WHERE a.status = 'ACTIVE' OR a.status = 'SCHEDULED'")
    List<Auction> findAvailableAuctions();

    // Buscar subastas por creador
    List<Auction> findByCreator(User creator);

    // Buscar subastas en las que un usuario está registrado
    @Query("SELECT a FROM Auction a JOIN a.registeredUsers u WHERE u = :user")
    List<Auction> findByRegisteredUser(@Param("user") User user);

    // Buscar subastas por rango de precio inicial del artículo
    @Query("SELECT a FROM Auction a WHERE a.article.initialPrice BETWEEN :minPrice AND :maxPrice")
    List<Auction> findByArticlePriceRange(@Param("minPrice") double minPrice, @Param("maxPrice") double maxPrice);

    // Buscar subastas que empiezan en un rango de fechas
    List<Auction> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    // Buscar subastas por estado y ordenadas por fecha de inicio
    List<Auction> findByStatusOrderByStartTimeDesc(Auction.AuctionStatus status);

    // Contar subastas activas de un usuario
    @Query("SELECT COUNT(a) FROM Auction a WHERE a.creator = :user AND a.status = 'ACTIVE'")
    long countActiveAuctionsByUser(@Param("user") User user);

    // Buscar subastas que coincidan con un término de búsqueda en el nombre del artículo
    @Query("SELECT a FROM Auction a WHERE LOWER(a.article.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Auction> searchByArticleName(@Param("searchTerm") String searchTerm);

    // Buscar subastas próximas a empezar en las próximas N horas
    @Query("SELECT a FROM Auction a WHERE a.status = 'SCHEDULED' AND a.startTime BETWEEN :now AND :future")
    List<Auction> findUpcomingAuctions(@Param("now") LocalDateTime now, @Param("future") LocalDateTime future);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Auction a WHERE a.id = :auctionId")
    Optional<Auction> findAuctionForUpdate(@Param("auctionId") Long auctionId);
}