package com.clinicanuevomilenio.equipamientos.dto;

import lombok.Data;

@Data
public class ConsumoStockDTO {
    // El ID del registro de inventario (de la tabla detalle_equipamiento)
    private Integer detalleId;
    // La cantidad a consumir o reponer
    private int cantidad;
}
