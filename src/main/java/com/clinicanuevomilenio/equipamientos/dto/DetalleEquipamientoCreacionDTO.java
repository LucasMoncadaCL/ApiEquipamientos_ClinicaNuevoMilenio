package com.clinicanuevomilenio.equipamientos.dto;

import lombok.Data;

@Data
public class DetalleEquipamientoCreacionDTO {
    private Integer equipamientoId; // A qué equipo pertenece
    private Integer pabellonId;     // En qué pabellón está
    private int cantidad;
    private int stockMinimo;
    private int stockMaximo;
}