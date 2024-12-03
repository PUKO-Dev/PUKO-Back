package edu.escuelaing.arsw.puko.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BidTest {

    @Test
    void testToString() {
        // Preparar los datos de prueba
        Long bidId = 1L;
        double amount = 200.0;
        LocalDateTime bidTime = LocalDateTime.of(2024, 12, 2, 15, 30, 0); // Ejemplo de fecha y hora
        User user = new User("testUser", "password123", "testuser@example.com", 10000.0); // Asumiendo que tienes un constructor adecuado para User
        Auction auction = new Auction(1L, user); // Asumiendo que tienes un constructor adecuado para Auction

        // Crear el objeto Bid
        Bid bid = new Bid();
        bid.setId(bidId);
        bid.setAmount(amount);
        bid.setBidTime(bidTime);
        bid.setUser(user);
        bid.setAuction(auction);

        // Crear el valor esperado para el toString
        String expected = "Bid{" +
                "id=" + bidId +
                ", auction=" + auction +
                ", user=" + user +
                ", amount=" + amount +
                ", bidTime=" + bidTime +
                '}';

        // Verificar que el método toString() devuelve la cadena esperada
        assertEquals(expected, bid.toString(), "El toString() de Bid no devuelve el formato esperado.");
    }
}