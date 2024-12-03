package edu.escuelaing.arsw.puko.config;

import edu.escuelaing.arsw.puko.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
public class BasicAuthSecurity {

    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomUserDetailsService userDetailsService;

    @Autowired
    public BasicAuthSecurity(RestAuthenticationEntryPoint authenticationEntryPoint, CustomUserDetailsService userDetailsService) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/public/**", "/api/**") // Define qué rutas están protegidas
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/public/**", "/stompend/**", "/negotiate/**").permitAll() //Permite acceso a todas las rutas que coinciden con /public/** sin autenticación, mientras que cualquier otra solicitud requiere autenticación.
                        .requestMatchers(HttpMethod.OPTIONS).permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> httpBasic
                        .authenticationEntryPoint(authenticationEntryPoint)
                )
                .csrf(AbstractHttpConfigurer::disable); // Deshabilitar CSRF
        if (System.getProperty("spring.profiles.active") != null && System.getProperty("spring.profiles.active").equals("test")) {
            http.authorizeRequests().anyRequest().permitAll(); // Permitir todas las solicitudes en pruebas
        }
        return http.build();
    }
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth, PasswordEncoder passwordEncoder) throws Exception {
        // Use the CustomUserDetailsService for user authentication
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }

}
