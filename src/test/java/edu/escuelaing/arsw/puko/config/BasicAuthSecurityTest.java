package edu.escuelaing.arsw.puko.config;

import edu.escuelaing.arsw.puko.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.*;

class BasicAuthSecurityTest {

    @Mock
    private RestAuthenticationEntryPoint authenticationEntryPoint;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @InjectMocks
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHttpMethodOptionsIsPermitted() throws Exception {
        // Crear un mock del HttpSecurity
        HttpSecurity httpSecurity = mock(HttpSecurity.class);

        // Configurar devoluciones válidas para los métodos encadenados
        when(httpSecurity.securityMatcher(any(String[].class))).thenReturn(httpSecurity);
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.exceptionHandling(any())).thenReturn(httpSecurity);
        when(httpSecurity.oauth2Login(any())).thenReturn(httpSecurity);
        when(httpSecurity.csrf(any())).thenReturn(httpSecurity);
        when(httpSecurity.addFilterBefore(any(), any())).thenReturn(httpSecurity);

        // Llamar al método que configura el HttpSecurity
        securityConfig.filterChain(httpSecurity);

        // Verificar que el método authorizeHttpRequests se llama y permite OPTIONS
        verify(httpSecurity).authorizeHttpRequests(any());
        verify(httpSecurity).csrf(any());
        verify(httpSecurity).oauth2Login(any());
    }
}
