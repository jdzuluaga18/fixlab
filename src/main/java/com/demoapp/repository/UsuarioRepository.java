package com.demoapp.repository;

import com.demoapp.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
/**
 * Repositorio JPA para la entidad Usuario.
 *
 * Proporciona operaciones de acceso a datos para:
 *  - Buscar usuarios por nombre de usuario.
 *  - Verificar si un username ya existe en la base de datos.
 *
 * Es utilizado por los servicios de autenticación y registro
 * para validar credenciales y gestionar la creación de usuarios.
 */


public interface UsuarioRepository  extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);
}