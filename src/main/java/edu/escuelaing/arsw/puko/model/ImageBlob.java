package edu.escuelaing.arsw.puko.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ImageBlob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;

    private boolean isMain; // Indicates if this is the main image

    private String filename; // Stores the name of the image file

    @ManyToOne
    @JoinColumn(name = "article_id")
    private Article article;

    public ImageBlob(String url, boolean isMain, String filename, Article article) {
        this.url = url;
        this.isMain = isMain;
        this.filename = filename;
        this.article = article;
    }
}
