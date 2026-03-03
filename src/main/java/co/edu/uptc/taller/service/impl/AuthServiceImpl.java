package co.edu.uptc.taller.service.impl;

import co.edu.uptc.taller.dto.LoginRequest;
import co.edu.uptc.taller.dto.LoginResponse;
import co.edu.uptc.taller.dto.RegisterRequest;
import co.edu.uptc.taller.model.User;
import co.edu.uptc.taller.repository.UserRepository;
import co.edu.uptc.taller.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Implementación del servicio de autenticación.
 * Módulo separado que gestiona login y registro de usuarios.
 * 
 * Trade-offs:
 * - Seguridad: Passwords hasheados con BCrypt, validaciones estrictas
 * - Rendimiento: Consultas de existencia optimizadas por índices únicos
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Autentica un usuario verificando sus credenciales.
     * 
     * @param request Credenciales de login
     * @return Respuesta con datos del usuario autenticado
     * @throws RuntimeException si las credenciales son inválidas
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        // Busca usuario por username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        // Verifica que el usuario esté habilitado
        if (!user.isEnabled()) {
            throw new RuntimeException("User account is disabled");
        }

        // Verifica password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        // Construye respuesta exitosa
        return LoginResponse.builder()
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .message("Login successful")
                .success(true)
                .build();
    }

    /**
     * Registra un nuevo usuario en el sistema.
     * Trade-off: Validaciones exhaustivas vs velocidad de registro
     * 
     * @param request Datos del nuevo usuario
     * @return Usuario creado
     * @throws RuntimeException si el username o email ya existen
     */
    @Override
    // @Transactional // Deshabilitado: requiere MongoDB Replica Set
    public User register(RegisterRequest request) {
        // Valida que el username no esté en uso
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Valida que el email no esté en uso
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Crea nuevo usuario con rol USER por defecto
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword())) // Hash BCrypt
                .email(request.getEmail())
                .fullName(request.getFullName())
                .role("USER") // Rol por defecto para nuevos usuarios
                .enabled(true) // Habilitado por defecto
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return userRepository.save(user);
    }

    /**
     * Verifica si un username ya está en uso.
     * Consulta optimizada por índice único en username.
     * 
     * @param username Username a verificar
     * @return true si ya existe
     */
    @Override
    public boolean isUsernameTaken(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Verifica si un email ya está en uso.
     * Consulta optimizada por índice en email.
     * 
     * @param email Email a verificar
     * @return true si ya existe
     */
    @Override
    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email);
    }
}
