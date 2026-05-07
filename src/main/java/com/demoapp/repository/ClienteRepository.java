package com.demoapp.repository;

import com.demoapp.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByNumPhone(String numPhone);
    Optional<Cliente> findByEmail(String email);
    boolean existsByNumPhone(String numPhone);
    boolean existsByEmail(String email);

    // Búsqueda simple por nombre o apellidos (ya existía)
    List<Cliente> findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCase(
            String name, String surname);

    // Búsqueda potente: nombre, apellidos, teléfono, email, dirección o notas
    @Query("""
        SELECT c FROM Cliente c WHERE
            LOWER(c.name)     LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(c.surname)  LIKE LOWER(CONCAT('%', :q, '%')) OR
            c.numPhone        LIKE CONCAT('%', :q, '%')        OR
            LOWER(c.email)    LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(c.direccion)LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(c.notas)    LIKE LOWER(CONCAT('%', :q, '%'))
        ORDER BY c.surname ASC, c.name ASC
    """)
    List<Cliente> buscarGlobal(@Param("q") String query);
}
