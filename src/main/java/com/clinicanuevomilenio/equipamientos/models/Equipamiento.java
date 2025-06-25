package com.clinicanuevomilenio.equipamientos.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "equipamiento")
@Data
public class Equipamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equipo_id")
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "descripcion", nullable = false, length = 100)
    private String descripcion;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado;

    @Column(name = "fecha_adquisicion")
    private LocalDate fechaAdquisicion;

    @Column(name = "vida_util")
    private Integer vidaUtil;

    // Relación inversa: Un tipo de equipo puede estar en muchos detalles de inventario (en diferentes pabellones)
    @OneToMany(mappedBy = "equipamiento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetalleEquipamiento> detalles;
}