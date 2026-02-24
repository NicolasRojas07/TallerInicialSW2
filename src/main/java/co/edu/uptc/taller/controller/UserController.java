package co.edu.uptc.taller.controller;

import co.edu.uptc.taller.dto.CreateUserRequest;
import co.edu.uptc.taller.dto.UserResponse;
import co.edu.uptc.taller.model.User;
import co.edu.uptc.taller.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para gestión de usuarios.
 * 
 * Endpoints:
 * - POST /api/users - Crear usuario
 * - GET /api/users - Listar todos los usuarios
 * - GET /api/users/{id} - Obtener usuario por ID
 * - GET /api/users/username/{username} - Obtener usuario por username
 * - PUT /api/users/{id} - Actualizar usuario
 * - DELETE /api/users/{id} - Eliminar usuario
 * - PATCH /api/users/{id}/enabled - Habilitar/deshabilitar usuario
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * Crea un nuevo usuario en el sistema.
     * 
     * @param request Datos del usuario a crear
     * @return Usuario creado (sin contraseña)
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.fromUser(user));
    }
    
    /**
     * Obtiene todos los usuarios del sistema.
     * 
     * @return Lista de usuarios
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserResponse> responses = users.stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Obtiene un usuario por su ID.
     * 
     * @param id ID del usuario
     * @return Usuario encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(UserResponse.fromUser(user)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Obtiene un usuario por su username.
     * 
     * @param username Username del usuario
     * @return Usuario encontrado
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username)
                .map(user -> ResponseEntity.ok(UserResponse.fromUser(user)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Actualiza un usuario existente.
     * 
     * @param id ID del usuario
     * @param request Datos actualizados
     * @return Usuario actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String id,
            @Valid @RequestBody CreateUserRequest request) {
        User user = userService.updateUser(id, request);
        return ResponseEntity.ok(UserResponse.fromUser(user));
    }
    
    /**
     * Elimina un usuario del sistema.
     * 
     * @param id ID del usuario
     * @return Respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Habilita o deshabilita un usuario.
     * 
     * @param id ID del usuario
     * @param enabled Estado a establecer
     * @return Usuario actualizado
     */
    @PatchMapping("/{id}/enabled")
    public ResponseEntity<UserResponse> setUserEnabled(
            @PathVariable String id,
            @RequestParam boolean enabled) {
        User user = userService.setUserEnabled(id, enabled);
        return ResponseEntity.ok(UserResponse.fromUser(user));
    }
}
