package edu.escuelaing.arsw.puko.dto;

import edu.escuelaing.arsw.puko.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserTokenDTO {
    private Long id;
    private String username;
    private String token;
    private User.AuthProvider authProvider;

    public static UserTokenDTO fromUser(User user, String token) {
        return new UserTokenDTO(user.getId(), user.getUsername(), token, user.getAuthProvider());
    }
}

