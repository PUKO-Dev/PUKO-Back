package edu.escuelaing.arsw.puko.controller;

import edu.escuelaing.arsw.puko.config.Encryption;
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
    public ResponseEntity<String> authenticateWithGoogle(@RequestBody Map<String, String> payload) {
        try {
            String idToken = payload.get("id_token");
            if (idToken == null || idToken.isEmpty()) {
                return ResponseEntity.badRequest().body("Token ID no válido");
            }

            Map<String, Object> data = googleAuthService.authenticateWithGoogle(idToken);
            return ResponseEntity.ok(Encryption.encrypt(UserTokenDTO.fromUser((User) data.get("user"), (String) data.get("token")).toString()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno del servidor");
        }
    }
}
