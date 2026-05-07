package com.demoapp.service;

import com.demoapp.model.Usuario;
import com.demoapp.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio encargado de gestionar la lógica de negocio relacionada con los usuarios.
 *
 * Proporciona operaciones para:
 *  - Autenticar usuarios mediante verificación de contraseña hasheada.
 *  - Registrar nuevos usuarios aplicando normalización y validaciones.
 *  - Consultar todos los usuarios registrados.
 *
 * Incluye un BCryptPasswordEncoder para garantizar que las contraseñas
 * se almacenen de forma segura y nunca en texto plano.
 *
 * Actúa como capa intermedia entre los controladores y el repositorio,
 * centralizando las reglas de negocio de autenticación y gestión de usuarios.
 */

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Optional<Usuario> login(String username, String password) {

        String userNormalizado = normalizar(username);

        return usuarioRepository.findByUsername(userNormalizado)
                .filter(u -> passwordEncoder.matches(password, u.getPassword()));
    }

    public void register(String username, String pass, Usuario.Rol rol) {

        String userNormalizado = normalizar(username);

        // 1. Comprobar si el usuario ya existe
        if (usuarioRepository.existsByUsername(userNormalizado)) {
            throw new RuntimeException("El usuario ya existe");
        }

        // 2. Crear el usuario
        Usuario usuario = new Usuario();
        usuario.setUsername(userNormalizado);
        usuario.setPassword(passwordEncoder.encode(pass));
        usuario.setRol(rol);

        // 3. Guardarlo en la base de datos
        usuarioRepository.save(usuario);
    }

    // Función para normalizar los nombres de la base de datos
    private String normalizar(String username) {
        if (username == null) return "";
        return username.trim().toLowerCase();
    }

    public List<Usuario> findAll() {

        return usuarioRepository.findAll();
    }
}