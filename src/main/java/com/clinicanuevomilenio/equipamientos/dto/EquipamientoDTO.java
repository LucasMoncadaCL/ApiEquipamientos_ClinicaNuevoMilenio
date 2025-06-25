package com.clinicanuevomilenio.equipamientos.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class EquipamientoDTO {
    private Integer id;
    private String nombre;
    private String descripcion;
    private String estado;
    private LocalDate fechaAdquisicion;
}