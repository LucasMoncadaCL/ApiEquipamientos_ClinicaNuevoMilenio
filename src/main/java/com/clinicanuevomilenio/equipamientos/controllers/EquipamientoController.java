package com.clinicanuevomilenio.equipamientos.controllers;

import com.clinicanuevomilenio.equipamientos.dto.*;
import com.clinicanuevomilenio.equipamientos.services.EquipamientoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/equipamiento")
@RequiredArgsConstructor
public class EquipamientoController {

    private final EquipamientoService equipamientoService;

    // --- Endpoints para el Catálogo General de Equipos ---

    @PostMapping("/tipos")
    public ResponseEntity<EquipamientoDTO> crearTipoDeEquipamiento(@RequestBody EquipamientoCreacionDTO dto) {
        EquipamientoDTO nuevoEquipo = equipamientoService.crearTipoEquipamiento(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoEquipo);
    }

    @GetMapping("/tipos")
    public ResponseEntity<List<EquipamientoDTO>> listarTiposDeEquipamiento() {
        return ResponseEntity.ok(equipamientoService.listarTiposEquipamiento());
    }

    // --- Endpoints para el Inventario/Stock en Pabellones ---

    @PostMapping("/stock")
    public ResponseEntity<?> agregarStockAPabellon(@RequestBody DetalleEquipamientoCreacionDTO dto) {
        try {
            DetalleEquipamientoRespuestaDTO nuevoStock = equipamientoService.agregarStock(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoStock);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/stock/pabellon/{pabellonId}")
    public ResponseEntity<?> obtenerStockPorPabellon(@PathVariable Integer pabellonId) {
        try {
            List<DetalleEquipamientoRespuestaDTO> stock = equipamientoService.obtenerEquipamientoPorPabellon(pabellonId);
            return ResponseEntity.ok(stock);
        } catch (EntityNotFoundException e) {
            // Este error ocurrirá si el pabellonId no existe en la pabellones-api.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/stock/consumir")
    public ResponseEntity<?> consumirStock(@RequestBody ConsumoStockDTO dto) {
        try {
            DetalleEquipamientoRespuestaDTO stockActualizado = equipamientoService.consumirStock(dto);
            return ResponseEntity.ok(stockActualizado);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/stock/pabellon/{pabellonId}/equipo/{equipamientoId}")
    public ResponseEntity<?> obtenerStockEspecifico(@PathVariable Integer pabellonId, @PathVariable Integer equipamientoId) {
        try {
            DetalleEquipamientoRespuestaDTO detalle = equipamientoService.obtenerDetalleEspecifico(pabellonId, equipamientoId);
            return ResponseEntity.ok(detalle);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}