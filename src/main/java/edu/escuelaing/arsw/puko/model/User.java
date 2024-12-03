package edu.escuelaing.arsw.puko.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor // Default constructor for JPA
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    @NotBlank(message = "Username is required")
    private String username;

    @JsonIgnore
    @NotBlank(message = "Password is required")
    private String password;

    @Column(unique = true)
    @Email(message = "Email should be valid")
    private String email;

    private double realMoney = 10000000; // Dinero real predeterminado
    private double temporaryMoney = realMoney; // Dinero temporal inicializado con el dinero real

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password; // This should be encrypted when setting
        this.email = email;
    }

    public User(String username, String password, String email, double realMoney) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.realMoney = realMoney;
        this.temporaryMoney = realMoney; // Inicializa el dinero temporal igual al real
    }

    public User(Long id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", realMoney=" + realMoney +
                ", temporaryMoney=" + temporaryMoney +
                '}';
    }
}
