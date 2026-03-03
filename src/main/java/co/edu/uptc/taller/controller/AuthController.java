package co.edu.uptc.taller.controller;

import co.edu.uptc.taller.dto.LoginRequest;
import co.edu.uptc.taller.dto.LoginResponse;
import co.edu.uptc.taller.dto.RegisterRequest;
import co.edu.uptc.taller.dto.UserResponse;
import co.edu.uptc.taller.model.User;
import co.edu.uptc.taller.service.AuthService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para módulo de autenticación.
 * Gestiona login y registro de usuarios de forma independiente del sistema bancario.
 * 
 * Endpoints públicos (no requieren autenticación):
 * - POST /api/auth/login
 * - POST /api/auth/register
 * - GET /api/auth/check-username
 * - GET /api/auth/check-email
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Endpoint de login.
     * Autentica un usuario con username y password.
     * 
     * @param request Credenciales de login
     * @return Respuesta con información del usuario autenticado
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            LoginResponse errorResponse = LoginResponse.builder()
                    .message(e.getMessage())
                    .success(false)
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * Endpoint de registro de nuevos usuarios.
     * Crea un nuevo usuario con rol USER por defecto.
     * 
     * @param request Datos del nuevo usuario
     * @return Usuario creado (sin exponer password)
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = authService.register(request);
            
            // Convierte a DTO sin exponer password
            UserResponse response = UserResponse.fromUser(user);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("success", "false");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Verifica si un username está disponible.
     * Útil para validación en tiempo real en el formulario de registro.
     * 
     * @param username Username a verificar
     * @return Mapa con disponibilidad
     */
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam String username) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", !authService.isUsernameTaken(username));
        return ResponseEntity.ok(response);
    }

    /**
     * Verifica si un email está disponible.
     * Útil para validación en tiempo real en el formulario de registro.
     * 
     * @param email Email a verificar
     * @return Mapa con disponibilidad
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", !authService.isEmailTaken(email));
        return ResponseEntity.ok(response);
    }
}
