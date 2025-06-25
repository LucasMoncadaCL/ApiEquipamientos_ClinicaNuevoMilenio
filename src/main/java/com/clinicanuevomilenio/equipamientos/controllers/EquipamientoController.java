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

    /**
     * Crea un nuevo tipo de equipamiento en el catálogo.
     * POST /api/equipamiento/tipos
     */
    @PostMapping("/tipos")
    public ResponseEntity<EquipamientoDTO> crearTipoDeEquipamiento(@RequestBody EquipamientoCreacionDTO dto) {
        EquipamientoDTO nuevoEquipo = equipamientoService.crearTipoEquipamiento(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoEquipo);
    }

    /**
     * Lista todos los tipos de equipamiento disponibles en el catálogo.
     * GET /api/equipamiento/tipos
     */
    @GetMapping("/tipos")
    public ResponseEntity<List<EquipamientoDTO>> listarTiposDeEquipamiento() {
        return ResponseEntity.ok(equipamientoService.listarTiposEquipamiento());
    }

    /**
     * Obtiene los detalles de un único tipo de equipamiento por su ID.
     * GET /api/equipamiento/tipos/{id}
     */
    @GetMapping("/tipos/{id}") // Este endpoint es llamado por EquipamientoClientService.obtenerEquipamientoPorId
    public ResponseEntity<?> obtenerTipoDeEquipamientoPorId(@PathVariable Integer id) {
        EquipamientoDTO equipamiento = equipamientoService.obtenerEquipamientoPorId(id);
        if (equipamiento == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Tipo de equipamiento no encontrado con ID: " + id));
        }
        return ResponseEntity.ok(equipamiento);
    }


    /**
     * Obtiene una lista de tipos de equipamiento a partir de una lista de IDs.
     * Este endpoint es el que el microservicio `solicitudservicio` intentaba llamar.
     * GET /api/equipamiento/tipos/por-ids?ids=1,2,3
     */
    @GetMapping("/tipos/por-ids")
    public ResponseEntity<List<EquipamientoDTO>> obtenerTiposDeEquipamientoPorIds(@RequestParam List<Integer> ids) {
        List<EquipamientoDTO> equipos = equipamientoService.obtenerTiposEquipamientoPorIds(ids);
        return ResponseEntity.ok(equipos);
    }

    // --- Endpoints para el Inventario/Stock en Pabellones ---

    /**
     * Agrega o actualiza el stock de un tipo de equipamiento en un pabellón específico.
     * POST /api/equipamiento/stock
     */
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

    /**
     * Obtiene todo el equipamiento en stock para un pabellón dado.
     * GET /api/equipamiento/stock/pabellon/{pabellonId}
     */
    @GetMapping("/stock/pabellon/{pabellonId}")
    public ResponseEntity<?> obtenerStockPorPabellon(@PathVariable Integer pabellonId) {
        try {
            List<DetalleEquipamientoRespuestaDTO> stock = equipamientoService.obtenerEquipamientoPorPabellon(pabellonId);
            return ResponseEntity.ok(stock);
        } catch (EntityNotFoundException e) {
            // Este error ocurrirá si el pabellonId no existe en la pabellones-api, o si no hay stock para ese pabellón.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Consume (reduce) una cantidad específica de un ítem de stock.
     * POST /api/equipamiento/stock/consumir
     */
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

    /**
     * Obtiene los detalles de stock para un tipo de equipamiento específico en un pabellón dado.
     * GET /api/equipamiento/stock/pabellon/{pabellonId}/equipo/{equipamientoId}
     */
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