package edu.escuelaing.arsw.puko.controller;

import com.azure.messaging.webpubsub.WebPubSubServiceClient;
import com.azure.messaging.webpubsub.WebPubSubServiceClientBuilder;
import com.azure.messaging.webpubsub.models.GetClientAccessTokenOptions;
import com.azure.messaging.webpubsub.models.WebPubSubClientAccessToken;
import com.google.gson.JsonObject;
import edu.escuelaing.arsw.puko.config.Encryption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebPubSubController {

    private final WebPubSubServiceClient service;

    public WebPubSubController(@Value("${webpubsub.connection-string}")
                               String connectionString) {
        // Construir el cliente del servicio WebPubSub
        this.service = new WebPubSubServiceClientBuilder()
                .connectionString(connectionString)
                .hub("puko")
                .buildClient();
    }

    @GetMapping("/negotiate")
    public String negotiate(@RequestParam String id) {
        if (id == null || id.isEmpty()) {
            return "{ \"error\": \"Missing user id\" }";
        }

        // Configurar las opciones para el token
        GetClientAccessTokenOptions options = new GetClientAccessTokenOptions()
                .setUserId(id)
                .addRole("webpubsub.joinLeaveGroup") // Permiso para unirse a grupos
                .addRole("webpubsub.sendToGroup");  // Permiso para enviar mensajes a grupos

        // Obtener el token de acceso para el cliente
        WebPubSubClientAccessToken token = service.getClientAccessToken(options);

        // Crear la respuesta con la URL del WebSocket y el token
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("url", token.getUrl());

        // Convertir la respuesta a String (JSON) antes de cifrarla
        String responseString = jsonResponse.toString();

        try {
            // Cifrar la respuesta con AES utilizando la clase Encryption
            return Encryption.encrypt(responseString);
        } catch (Exception e) {
            e.printStackTrace();
            return "{ \"error\": \"Encryption fail\" }";
        }
    }
}