package com.clinicanuevomilenio.equipamientos.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "detalle_equipamiento", uniqueConstraints = {
        // Asegura que no se pueda duplicar el mismo equipo en el mismo pabellón
        @UniqueConstraint(columnNames = {"EQUIPAMIENTO_equipo_id", "PABELLON_pabellon_id"})
})
@Data
public class DetalleEquipamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detalle_id")
    private Integer id;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo;

    @Column(name = "stock_maximo", nullable = false)
    private Integer stockMaximo;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDate fechaActualizacion;

    // --- Relaciones ---

    // Relación Muchos-a-Uno con Equipamiento. Esto está dentro del mismo microservicio.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EQUIPAMIENTO_equipo_id", nullable = false)
    private Equipamiento equipamiento;

    // --- PUNTO DE ARQUITECTURA IMPORTANTE ---
    // En lugar de enlazar a una entidad Pabellon, solo guardamos el ID.
    @Column(name = "PABELLON_pabellon_id", nullable = false)
    private Integer pabellonId;
}