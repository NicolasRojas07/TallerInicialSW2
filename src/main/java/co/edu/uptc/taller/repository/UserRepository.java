package co.edu.uptc.taller.repository;

import co.edu.uptc.taller.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para operaciones de persistencia de usuarios.
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {
    
    /**
     * Busca un usuario por su username.
     * 
     * @param username Username a buscar
     * @return Optional con el usuario si existe
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Busca un usuario por su email.
     * 
     * @param email Email a buscar
     * @return Optional con el usuario si existe
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Verifica si existe un usuario con el username dado.
     * 
     * @param username Username a verificar
     * @return true si existe
     */
    boolean existsByUsername(String username);
    
    /**
     * Verifica si existe un usuario con el email dado.
     * 
     * @param email Email a verificar
     * @return true si existe
     */
    boolean existsByEmail(String email);
}
