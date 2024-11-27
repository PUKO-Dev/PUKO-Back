package edu.escuelaing.arsw.puko;

import edu.escuelaing.arsw.puko.model.User;
import edu.escuelaing.arsw.puko.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Solo agregar usuarios si no existen
        if (userRepository.count() == 0) {
            // Crear y codificar usuarios
            User juli = new User("juli", passwordEncoder.encode("julipass"), "juli@example.com");
            User cris = new User("cris", passwordEncoder.encode("crispass"), "cris@example.com");
            User nat = new User("nat", passwordEncoder.encode("natpass"), "nat@example.com");
            User diego = new User("diego", passwordEncoder.encode("diegopass"), "diego@example.com");
            User user1 = new User("user1", passwordEncoder.encode("user1pass"), "user1@example.com");

            userRepository.save(juli);
            userRepository.save(cris);
            userRepository.save(nat);
            userRepository.save(diego);
            userRepository.save(user1);
        }
    }
}

