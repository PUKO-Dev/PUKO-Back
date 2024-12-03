package edu.escuelaing.arsw.puko.controller;

import edu.escuelaing.arsw.puko.dto.UserTokenDTO;
import edu.escuelaing.arsw.puko.model.User;
import edu.escuelaing.arsw.puko.service.GoogleAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;

    @Autowired
    public GoogleAuthController(GoogleAuthService googleAuthService) {
        this.googleAuthService = googleAuthService;
    }

    @PostMapping("/google")
    public ResponseEntity<?> authenticateWithGoogle(@RequestBody Map<String, String> payload) {
        try {
            String idToken = payload.get("id_token");
            if (idToken == null || idToken.isEmpty()) {
                System.out.println("Token ID no válido");
                return ResponseEntity.badRequest().body("Token ID no válido");
            }

            Map<String, Object> data = googleAuthService.authenticateWithGoogle(idToken);
            return ResponseEntity.ok(UserTokenDTO.fromUser((User) data.get("user"), (String) data.get("token")));
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(500).body("Error interno del servidor");
        }
    }
}
