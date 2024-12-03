package edu.escuelaing.arsw.puko;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class PukoApplicationTest {

    @Test
    void contextLoads() {
        // Verifica que el contexto de Spring Boot se cargue correctamente.
        assertTrue(true, "El contexto de Spring Boot se cargó correctamente.");
    }
}
