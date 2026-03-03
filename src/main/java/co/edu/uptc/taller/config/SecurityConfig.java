package co.edu.uptc.taller.config;

import co.edu.uptc.taller.service.impl.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Configuracion de seguridad para la aplicacion.
 * 
 * Trade-offs considerados:
 * - Autenticacion HTTP Basic: Simple pero requiere HTTPS en produccion - Trade-off: seguridad
 * - Sesiones stateless: Mejor rendimiento para alta concurrencia - Trade-off: rendimiento
 * - CORS habilitado: Permite frontend separado pero aumenta superficie de ataque - Trade-off: funcionalidad vs seguridad
 * - Passwords hasheados con BCrypt: Seguridad fuerte pero costo computacional - Trade-off: seguridad vs rendimiento
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * Configura la cadena de filtros de seguridad.
     * 
     * Decisiones de seguridad:
     * - Todos los endpoints requieren autenticacion (excepto frontend estatico)
     * - CORS habilitado para permitir frontend
     * - CSRF deshabilitado por ser API REST stateless
     * - Sesiones stateless para mejor rendimiento con alta concurrencia
     * - HTTP Basic auth para simplicidad (DEBE usarse HTTPS en produccion)
     * 
     * @param http Configurador de seguridad HTTP
     * @return Cadena de filtros configurada
     * @throws Exception si hay error en configuracion
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                // CSRF deshabilitado para API REST stateless - Trade-off: rendimiento vs seguridad
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(requests -> requests
                        // Permite acceso publico al frontend
                        .requestMatchers("/", "/index.html", "/styles.css", "/app.js").permitAll()
                        // Todos los endpoints de API requieren autenticacion
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated())
                // HTTP Basic Authentication - simple pero efectiva con HTTPS
                .httpBasic(withDefaults())
                // Sesiones stateless para mejor rendimiento con alta concurrencia - Trade-off: rendimiento
                .sessionManagement(management -> management
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    /**
     * Configura el encoder de passwords.
     * BCrypt con factor de trabajo 10 (default).
     * 
     * Trade-off: Seguridad fuerte (resistente a ataques de fuerza bruta) 
     * vs Rendimiento (costo computacional en cada login).
     * 
     * @return Encoder BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura el proveedor de autenticacion con UserDetailsService y PasswordEncoder.
     * 
     * @return Proveedor de autenticacion configurado
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Bean de AuthenticationManager para Spring Security.
     * 
     * @param authConfig Configuracion de autenticacion
     * @return AuthenticationManager
     * @throws Exception si hay error en configuracion
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Configura CORS para permitir peticiones desde frontend.
     * 
     * Trade-off: Funcionalidad (permite frontend separado) vs 
     * Seguridad (aumenta superficie de ataque).
     * 
     * En produccion: restringir origins a dominios especificos.
     * 
     * @return Configuracion de CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // En produccion: reemplazar "*" con dominio especifico
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
