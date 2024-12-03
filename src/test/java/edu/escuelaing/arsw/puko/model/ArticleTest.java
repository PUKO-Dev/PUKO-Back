package edu.escuelaing.arsw.puko.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArticleTest {

    @Test
    void testToString() {
        // Valores de prueba
        Long id = 1L;
        String name = "ArticleName";
        User user = new User("testUser", "password123", "testuser@example.com", 10000.0); // Asumiendo que tienes un constructor adecuado para User
        double initialPrice = 500.0;

        // Crear el objeto Article
        Article article = new Article(name, user, initialPrice);
        article.setId(id);  // Asignar el id, que normalmente lo establece la base de datos

        // Obtener la cadena esperada
        String expected = "Article{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", user=" + user + // El user será una cadena representativa del objeto User
                ", initialPrice=" + initialPrice +
                '}';

        // Verificar que el método toString() sea correcto
        assertEquals(expected, article.toString(), "El toString() de Article no devuelve el formato esperado.");
    }
}