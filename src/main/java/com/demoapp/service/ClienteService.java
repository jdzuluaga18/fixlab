package com.demoapp.service;

import com.demoapp.model.Cliente;
import com.demoapp.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
/**
 * Servicio encargado de gestionar la lógica de negocio relacionada con los clientes.
 *
 * Proporciona operaciones para:
 *  - Crear, actualizar, buscar y eliminar clientes.
 *  - Validar datos únicos como teléfono y email.
 *  - Realizar búsquedas parciales por nombre o apellidos.
 *  - Verificar el formato del email antes de guardar.
 *
 * Actúa como capa intermedia entre los controladores y el repositorio,
 * centralizando las reglas de negocio y garantizando la integridad de los datos.
 */

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    // Expresión regular para validar el formato del email
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    //  Obtener todos los clientes
    public List<Cliente> findAll() {
        return clienteRepository.findAll();
    }

    // ── Buscar por ID ─────────────────────────────────────
    public Optional<Cliente> findById(Long id) {
        return clienteRepository.findById(id);
    }

    // ── Búsqueda potente: nombre, apellidos, teléfono, email, dirección, notas ──
    public List<Cliente> buscar(String texto) {
        if (texto == null || texto.isBlank()) {
            return clienteRepository.findAll();
        }
        return clienteRepository.buscarGlobal(texto.trim());
    }

    public Cliente crear(String name, String surname, String numPhone,
                         String email, String direccion, String notas) {

        validarEmail(email);

        if (clienteRepository.existsByNumPhone(numPhone)) {
            throw new RuntimeException("Ya existe un cliente con ese teléfono");
        }

        if (email != null && !email.isBlank() && clienteRepository.existsByEmail(email)) {
            throw new RuntimeException("Ya existe un cliente con ese email");
        }

        Cliente cliente = new Cliente();
        cliente.setName(name.trim());
        cliente.setSurname(surname.trim());
        cliente.setNumPhone(numPhone.trim());
        cliente.setEmail(email != null && !email.isBlank() ? email.trim() : null);
        cliente.setDireccion(direccion != null && !direccion.isBlank() ? direccion.trim() : null);
        cliente.setNotas(notas != null && !notas.isBlank() ? notas.trim() : null);

        return clienteRepository.save(cliente);
    }

    public Cliente actualizar(Long id, String name, String surname, String numPhone,
                              String email, String direccion, String notas) {

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        validarEmail(email);

        if (!cliente.getNumPhone().equals(numPhone) &&
                clienteRepository.existsByNumPhone(numPhone)) {
            throw new RuntimeException("Ya existe un cliente con ese teléfono");
        }

        if (email != null && !email.isBlank() &&
                !email.equals(cliente.getEmail()) &&
                clienteRepository.existsByEmail(email)) {
            throw new RuntimeException("Ya existe un cliente con ese email");
        }

        cliente.setName(name.trim());
        cliente.setSurname(surname.trim());
        cliente.setNumPhone(numPhone.trim());
        cliente.setEmail(email != null && !email.isBlank() ? email.trim() : null);
        cliente.setDireccion(direccion != null && !direccion.isBlank() ? direccion.trim() : null);
        cliente.setNotas(notas != null && !notas.isBlank() ? notas.trim() : null);

        return clienteRepository.save(cliente);
    }

    // Eliminar cliente
    public void eliminar(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new RuntimeException("Cliente no encontrado");
        }
        clienteRepository.deleteById(id);
    }

    // Validación de formato de email
    private void validarEmail(String email) {
        if (email != null && !email.isBlank() && !email.matches(EMAIL_REGEX)) {
            throw new RuntimeException("El formato del email no es válido (ejemplo: nombre@dominio.com)");
        }
    }
}