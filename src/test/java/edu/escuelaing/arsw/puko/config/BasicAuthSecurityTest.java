package edu.escuelaing.arsw.puko.config;
import edu.escuelaing.arsw.puko.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

class BasicAuthSecurityTest {

    @Mock
    private RestAuthenticationEntryPoint authenticationEntryPoint;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private AuthenticationManagerBuilder authBuilder;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpSecurity httpSecurity;  // Mocking HttpSecurity

    @InjectMocks
    private BasicAuthSecurity basicAuthSecurity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }



    @Test
    void testHttpMethodOptionsIsPermitted() throws Exception {
        // Simulamos el HttpSecurity para el método OPTIONS
        when(httpSecurity.securityMatcher(any(String[].class))).thenReturn(httpSecurity);
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.httpBasic(any())).thenReturn(httpSecurity);
        when(httpSecurity.csrf(any())).thenReturn(httpSecurity);

        basicAuthSecurity.filterChain(httpSecurity);

        // Verificamos que el método OPTIONS esté permitido
        verify(httpSecurity).authorizeHttpRequests(any());
    }
}