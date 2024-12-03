package edu.escuelaing.arsw.puko.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ArticleWithImagesDTO {
    private Long id;
    private String name;
    private Long userId;
    private double initialPrice;
    private List<String> imageUrls;

    public ArticleWithImagesDTO(Long id, String name, Long userId, double initialPrice, List<String> imageUrls) {
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.initialPrice = initialPrice;
        this.imageUrls = imageUrls;
    }

    // Getters y Setters
}

