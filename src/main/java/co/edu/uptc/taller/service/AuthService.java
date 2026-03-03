package co.edu.uptc.taller.service;

import co.edu.uptc.taller.dto.LoginRequest;
import co.edu.uptc.taller.dto.LoginResponse;
import co.edu.uptc.taller.dto.RegisterRequest;
import co.edu.uptc.taller.model.User;

/**
 * Servicio para gestión de autenticación y registro de usuarios.
 * Módulo separado del sistema bancario principal.
 */
public interface AuthService {

    /**
     * Autentica un usuario con sus credenciales.
     * 
     * @param request Credenciales de login
     * @return Respuesta con información del usuario autenticado
     */
    LoginResponse login(LoginRequest request);

    /**
     * Registra un nuevo usuario en el sistema.
     * El usuario se crea con rol USER por defecto.
     * 
     * @param request Datos del nuevo usuario
     * @return Usuario creado
     */
    User register(RegisterRequest request);

    /**
     * Verifica si un username ya está en uso.
     * 
     * @param username Username a verificar
     * @return true si ya existe, false si está disponible
     */
    boolean isUsernameTaken(String username);

    /**
     * Verifica si un email ya está en uso.
     * 
     * @param email Email a verificar
     * @return true si ya existe, false si está disponible
     */
    boolean isEmailTaken(String email);
}
