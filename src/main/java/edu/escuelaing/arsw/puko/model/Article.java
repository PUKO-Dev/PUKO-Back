package edu.escuelaing.arsw.puko.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ImageBlob> images;

    private double initialPrice;

    @Column
    private boolean inAuction = false;

    public Article(String name, User user, double initialPrice) {
        this.name = name;
        this.user = user;
        this.initialPrice = initialPrice;
    }

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", user=" + user +
                ", initialPrice=" + initialPrice +
                '}';
    }
}
