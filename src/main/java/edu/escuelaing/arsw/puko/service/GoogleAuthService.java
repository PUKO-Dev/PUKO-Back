package edu.escuelaing.arsw.puko.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import edu.escuelaing.arsw.puko.config.JWTGenerator;
import edu.escuelaing.arsw.puko.model.User;
import edu.escuelaing.arsw.puko.repository.UserRepository;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class GoogleAuthService {

    private UserRepository userRepository;

    private JWTGenerator jwtGenerator;

    private GoogleIdTokenVerifier googleIdTokenVerifier;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String CLIENT_ID;

    @Autowired
    public GoogleAuthService(UserRepository userRepository, JWTGenerator jwtGenerator) {
        this.userRepository = userRepository;
        this.jwtGenerator = jwtGenerator;
        this.googleIdTokenVerifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                .setAudience(Collections.singletonList(CLIENT_ID))
                .build();
    }

    public Map<String, Object> authenticateWithGoogle(String idToken) throws GeneralSecurityException, IOException {
        GoogleIdToken.Payload payloadGoogle = validateGoogleIdToken(idToken);

        if (payloadGoogle == null) {
            throw new IllegalArgumentException("Token ID inválido");
        }

        String email = payloadGoogle.getEmail();
        User user = userRepository.findByEmail(email);

        if (user == null) {
            // Crear un nuevo usuario si no existe
            String name = payloadGoogle.get("name").toString();
            user = new User(name, User.AuthProvider.GOOGLE, email);
            userRepository.save(user);
        } else {
            // Sincronizar datos del usuario
            String googleName = payloadGoogle.get("name").toString();
            if (!googleName.equals(user.getUsername())) {
                user.setUsername(googleName);
            }
            // Sincroniza otros campos si es necesario
            userRepository.save(user);
        }

        // Generar el JWT para el usuario
        String token = jwtGenerator.generateTokenFromEmail(email);

        // Devolver token y usuario
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", user);
        return response;
    }


    // Método para validar el ID Token de Google
    private GoogleIdToken.Payload validateGoogleIdToken(String idToken) throws GeneralSecurityException, IOException {
        // Creando el verificador para el ID Token
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(CLIENT_ID)) // Establece el CLIENT_ID
                .build();

        // Verifica el ID Token
        GoogleIdToken googleIdToken = verifier.verify(idToken);
        if (googleIdToken != null) {
            // Si la validación es exitosa, obtener el payload del token
            return googleIdToken.getPayload();
        } else {
            // Si el ID Token no es válido
            return null;
        }
    }
}
