package edu.escuelaing.arsw.puko.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleWithImageDTO {

    private Long id;
    private String name;
    private Long userId;
    private String mainImage; // Imagen en formato Base64
    private double initialPrice;

    @Override
    public String toString() {
        return "{" +
                "\"id\":" + id +
                ", \"name\":\"" + name + "\"" +
                ", \"userId\":" + userId +
                ", \"mainImage\":\"" + (mainImage != null ? mainImage : "") + "\"" +
                ", \"initialPrice\":" + initialPrice +
                "}";
    }

}


