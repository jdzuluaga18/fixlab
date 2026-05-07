package com.demoapp.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Column(nullable = false, unique = true)
    private String numPhone;

    @Column(unique = true)
    private String email;

    @Column
    private String direccion;

    @Column(length = 2000)
    private String notas;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reparacion> reparaciones = new ArrayList<>();

    public Cliente() {}

    public Cliente(Long id, String name, String surname, String numPhone, String email, String direccion) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.numPhone = numPhone;
        this.email = email;
        this.direccion = direccion;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getNumPhone() { return numPhone; }
    public void setNumPhone(String numPhone) { this.numPhone = numPhone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public List<Reparacion> getReparaciones() { return reparaciones; }
    public void setReparaciones(List<Reparacion> reparaciones) { this.reparaciones = reparaciones; }

    public void addReparacion(Reparacion reparacion) {
        reparaciones.add(reparacion);
        reparacion.setCliente(this);
    }

    public void removeReparacion(Reparacion reparacion) {
        reparaciones.remove(reparacion);
        reparacion.setCliente(null);
    }
}
