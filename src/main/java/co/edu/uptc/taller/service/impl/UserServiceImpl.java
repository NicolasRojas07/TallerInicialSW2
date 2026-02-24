package co.edu.uptc.taller.service.impl;

import co.edu.uptc.taller.dto.CreateUserRequest;
import co.edu.uptc.taller.model.User;
import co.edu.uptc.taller.repository.UserRepository;
import co.edu.uptc.taller.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de gestión de usuarios.
 * 
 * Trade-offs considerados:
 * - Hasheo de contraseñas con BCrypt: Alta seguridad pero costo computacional - Trade-off: seguridad vs rendimiento
 * - Validación de unicidad de username y email: Previene duplicados pero requiere consultas adicionales - Trade-off: integridad vs rendimiento
 * - Índice único en username: Garantiza unicidad a nivel de BD pero puede afectar escrituras - Trade-off: consistencia vs rendimiento
 */
@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Crea un nuevo usuario validando que username y email sean únicos.
     * La contraseña se hashea con BCrypt antes de almacenar.
     * 
     * @param request Datos del usuario
     * @return Usuario creado
     * @throws RuntimeException si username o email ya existen
     */
    @Override
    public User createUser(CreateUserRequest request) {
        // Validar que el username no exista
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("El username '" + request.getUsername() + "' ya está en uso");
        }
        
        // Validar que el email no exista
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email '" + request.getEmail() + "' ya está registrado");
        }
        
        // Establecer rol por defecto si no se especifica
        String role = request.getRole();
        if (role == null || role.trim().isEmpty()) {
            role = "USER";
        }
        
        // Crear el usuario
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword())) // Hashear contraseña - Trade-off: seguridad
                .email(request.getEmail())
                .fullName(request.getFullName())
                .role(role)
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        return userRepository.save(user);
    }
    
    @Override
    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }
    
    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * Actualiza un usuario existente.
     * Si se proporciona una nueva contraseña, se hashea antes de guardar.
     * 
     * @param id ID del usuario
     * @param request Datos actualizados
     * @return Usuario actualizado
     * @throws RuntimeException si el usuario no existe o si username/email están en uso
     */
    @Override
    public User updateUser(String id, CreateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        
        // Validar username si cambió
        if (!user.getUsername().equals(request.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("El username '" + request.getUsername() + "' ya está en uso");
            }
            user.setUsername(request.getUsername());
        }
        
        // Validar email si cambió
        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("El email '" + request.getEmail() + "' ya está registrado");
            }
            user.setEmail(request.getEmail());
        }
        
        // Actualizar contraseña si se proporcionó
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        // Actualizar otros campos
        user.setFullName(request.getFullName());
        if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
            user.setRole(request.getRole());
        }
        user.setUpdatedAt(Instant.now());
        
        return userRepository.save(user);
    }
    
    @Override
    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }
        userRepository.deleteById(id);
    }
    
    @Override
    public User setUserEnabled(String id, boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        
        user.setEnabled(enabled);
        user.setUpdatedAt(Instant.now());
        
        return userRepository.save(user);
    }
}
