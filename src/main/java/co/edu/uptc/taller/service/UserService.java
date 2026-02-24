package co.edu.uptc.taller.service;

import co.edu.uptc.taller.dto.CreateUserRequest;
import co.edu.uptc.taller.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de usuarios.
 */
public interface UserService {
    
    /**
     * Crea un nuevo usuario en el sistema.
     * 
     * @param request Datos del usuario a crear
     * @return Usuario creado
     * @throws RuntimeException si el username o email ya existen
     */
    User createUser(CreateUserRequest request);
    
    /**
     * Obtiene un usuario por su ID.
     * 
     * @param id ID del usuario
     * @return Optional con el usuario si existe
     */
    Optional<User> getUserById(String id);
    
    /**
     * Obtiene un usuario por su username.
     * 
     * @param username Username del usuario
     * @return Optional con el usuario si existe
     */
    Optional<User> getUserByUsername(String username);
    
    /**
     * Obtiene todos los usuarios del sistema.
     * 
     * @return Lista de usuarios
     */
    List<User> getAllUsers();
    
    /**
     * Actualiza un usuario existente.
     * 
     * @param id ID del usuario
     * @param request Datos actualizados
     * @return Usuario actualizado
     */
    User updateUser(String id, CreateUserRequest request);
    
    /**
     * Elimina un usuario del sistema.
     * 
     * @param id ID del usuario a eliminar
     */
    void deleteUser(String id);
    
    /**
     * Habilita o deshabilita un usuario.
     * 
     * @param id ID del usuario
     * @param enabled Estado a establecer
     * @return Usuario actualizado
     */
    User setUserEnabled(String id, boolean enabled);
}
