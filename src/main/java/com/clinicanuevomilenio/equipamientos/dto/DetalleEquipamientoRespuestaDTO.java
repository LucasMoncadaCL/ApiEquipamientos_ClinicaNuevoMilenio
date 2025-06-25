package com.clinicanuevomilenio.equipamientos.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class DetalleEquipamientoRespuestaDTO {
    private Integer id; // ID del registro de detalle
    private int cantidad;
    private int stockMinimo;
    private int stockMaximo;
    private LocalDate fechaActualizacion;

    // --- Datos del Equipo (Anidados) ---
    private EquipamientoDTO equipamiento;

    // --- Datos del Pabellón (Enriquecidos) ---
    private Integer pabellonId;
    private String pabellonNombre; // Este dato lo obtendremos llamando a la pabellones-api
}