package co.edu.uptc.taller.config;

import co.edu.uptc.taller.Enums.AccountStatus;
import co.edu.uptc.taller.model.Account;
import co.edu.uptc.taller.model.User;
import co.edu.uptc.taller.repository.AccountRepository;
import co.edu.uptc.taller.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Configuracion para inicializar datos de prueba en desarrollo.
 * Solo se ejecuta con perfil "dev" para evitar contaminar produccion.
 * 
 * Crea cuentas de prueba para los usuarios configurados en SecurityConfig.
 */
@Configuration
@Profile("dev")
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Inicializa datos de prueba al arrancar la aplicacion.
     * Solo se ejecuta si el perfil activo es "dev".
     * 
     * Crea usuarios y cuentas para:
     * - user1: Usuario con cuenta de $1,000,000 COP
     * - user2: Usuario con cuenta de $2,500,000 COP
     * - admin: Administrador con cuenta de $10,000,000 COP
     */
    @Bean
    public CommandLineRunner initData() {
        return args -> {
            logger.info("Inicializando datos de prueba...");
            
            // Verifica si ya existen datos
            if (userRepository.count() > 0 && accountRepository.count() > 0) {
                logger.info("Ya existen datos, omitiendo inicializacion");
                return;
            }
            
            // Crear usuarios de prueba
            if (userRepository.count() == 0) {
                User user1 = User.builder()
                        .username("user1")
                        .password(passwordEncoder.encode("password"))
                        .email("user1@example.com")
                        .fullName("Usuario Uno")
                        .role("USER")
                        .enabled(true)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();
                userRepository.save(user1);
                logger.info("Usuario creado: user1");
                
                User user2 = User.builder()
                        .username("user2")
                        .password(passwordEncoder.encode("password"))
                        .email("user2@example.com")
                        .fullName("Usuario Dos")
                        .role("USER")
                        .enabled(true)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();
                userRepository.save(user2);
                logger.info("Usuario creado: user2");
                
                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .email("admin@example.com")
                        .fullName("Administrador")
                        .role("ADMIN")
                        .enabled(true)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();
                userRepository.save(admin);
                logger.info("Usuario creado: admin");
            }
            
            // Crear cuentas para los usuarios
            if (accountRepository.count() == 0) {
                // Crea cuenta para user1
                Account account1 = Account.builder()
                        .userId("user1")
                        .balance(new BigDecimal("1000000"))
                        .currency("COP")
                        .status(AccountStatus.ACTIVE)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();
                accountRepository.save(account1);
                logger.info("Cuenta creada para user1: {}", account1.getId());
                
                // Crea cuenta para user2
                Account account2 = Account.builder()
                        .userId("user2")
                        .balance(new BigDecimal("2500000"))
                        .currency("COP")
                        .status(AccountStatus.ACTIVE)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();
                accountRepository.save(account2);
                logger.info("Cuenta creada para user2: {}", account2.getId());
                
                // Crea cuenta para admin
                Account account3 = Account.builder()
                        .userId("admin")
                        .balance(new BigDecimal("10000000"))
                        .currency("COP")
                        .status(AccountStatus.ACTIVE)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();
                accountRepository.save(account3);
                logger.info("Cuenta creada para admin: {}", account3.getId());
            }
            
            logger.info("Datos de prueba inicializados exitosamente");
        };
    }
}
