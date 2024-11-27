package edu.escuelaing.arsw.puko.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ArticleWithImageDTO {

    private Long id;
    private String name;
    private Long userId;
    private String mainImage; // Imagen en formato Base64
    private double initialPrice;
}


