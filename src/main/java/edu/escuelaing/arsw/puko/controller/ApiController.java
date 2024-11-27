package edu.escuelaing.arsw.puko.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/secureData")
    public ResponseEntity<String> getSecureData() {
        return new ResponseEntity<>("Este es un dato seguro", HttpStatus.OK);
    }
}
