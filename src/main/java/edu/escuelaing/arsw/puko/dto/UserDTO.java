package edu.escuelaing.arsw.puko.dto;

import edu.escuelaing.arsw.puko.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;

    public static UserDTO fromUser(User user) {
        return new UserDTO(user.getId(), user.getUsername());
    }
}

