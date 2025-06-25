package com.clinicanuevomilenio.equipamientos.services;

import com.clinicanuevomilenio.equipamientos.dto.*;
import com.clinicanuevomilenio.equipamientos.models.DetalleEquipamiento;
import com.clinicanuevomilenio.equipamientos.models.Equipamiento;
import com.clinicanuevomilenio.equipamientos.repository.DetalleEquipamientoRepository;
import com.clinicanuevomilenio.equipamientos.repository.EquipamientoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipamientoService {

    private final EquipamientoRepository equipamientoRepository;
    private final DetalleEquipamientoRepository detalleRepository;
    private final WebClient pabellonWebClient;

    // --- Métodos de Catálogo ---

    @Transactional
    public EquipamientoDTO crearTipoEquipamiento(EquipamientoCreacionDTO dto) {
        Equipamiento equipamiento = new Equipamiento();
        equipamiento.setNombre(dto.getNombre());
        equipamiento.setDescripcion(dto.getDescripcion());
        equipamiento.setEstado(dto.getEstado());
        equipamiento.setFechaAdquisicion(LocalDate.now());
        Equipamiento guardado = equipamientoRepository.save(equipamiento);
        return convertirAEquipamientoDTO(guardado);
    }

    public List<EquipamientoDTO> listarTiposEquipamiento() {
        return equipamientoRepository.findAll().stream()
                .map(this::convertirAEquipamientoDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los detalles de un único equipamiento por su ID.
     * Utilizado por EquipamientoClientService.obtenerEquipamientoPorId.
     * @param equipamientoId El ID del equipamiento a buscar.
     * @return El DTO del equipamiento o null si no se encuentra.
     */
    public EquipamientoDTO obtenerEquipamientoPorId(Integer equipamientoId) {
        return equipamientoRepository.findById(equipamientoId)
                .map(this::convertirAEquipamientoDTO)
                .orElse(null); // Retorna null si no se encuentra, coherente con WebClient
    }

    /**
     * Obtiene una lista de tipos de equipamiento por una lista de sus IDs.
     * Crucial para la optimización N+1 en el microservicio `solicitudservicio`.
     * @param ids Lista de IDs de equipamiento a buscar.
     * @return Lista de DTOs de equipamiento. Retorna una lista vacía si no se encuentran coincidencias o si la lista de IDs es nula/vacía.
     */
    public List<EquipamientoDTO> obtenerTiposEquipamientoPorIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of(); // Retorna una lista vacía si no hay IDs
        }
        return equipamientoRepository.findAllById(ids).stream()
                .map(this::convertirAEquipamientoDTO)
                .collect(Collectors.toList());
    }

    // --- Métodos de Inventario ---

    @Transactional
    public DetalleEquipamientoRespuestaDTO agregarStock(DetalleEquipamientoCreacionDTO dto) {
        Equipamiento equipamiento = equipamientoRepository.findById(dto.getEquipamientoId())
                .orElseThrow(() -> new EntityNotFoundException("Equipamiento no encontrado con ID: " + dto.getEquipamientoId()));

        // Regla de Negocio: Validar estado del equipo para añadir stock
        if (!"Operativo".equals(equipamiento.getEstado())) {
            throw new IllegalStateException("No se puede gestionar stock de un equipo que no está 'Operativo'.");
        }

        PabellonRespuestaDTO pabellonDTO = validarPabellon(dto.getPabellonId());

        Optional<DetalleEquipamiento> detalleExistenteOpt = detalleRepository.findByEquipamientoIdAndPabellonId(
                dto.getEquipamientoId(), dto.getPabellonId());

        DetalleEquipamiento detalleParaGuardar;

        if (detalleExistenteOpt.isPresent()) {
            detalleParaGuardar = detalleExistenteOpt.get();
        } else {
            detalleParaGuardar = new DetalleEquipamiento();
            detalleParaGuardar.setEquipamiento(equipamiento);
            detalleParaGuardar.setPabellonId(pabellonDTO.getId());
        }

        detalleParaGuardar.setCantidad(dto.getCantidad());
        detalleParaGuardar.setStockMinimo(dto.getStockMinimo());
        detalleParaGuardar.setStockMaximo(dto.getStockMaximo());
        detalleParaGuardar.setFechaActualizacion(LocalDate.now());

        DetalleEquipamiento guardado = detalleRepository.save(detalleParaGuardar);
        return convertirADetalleRespuestaDTO(guardado, pabellonDTO.getNombre());
    }

    @Transactional(readOnly = true)
    public List<DetalleEquipamientoRespuestaDTO> obtenerEquipamientoPorPabellon(Integer pabellonId) {
        PabellonRespuestaDTO pabellonDTO = validarPabellon(pabellonId);
        List<DetalleEquipamiento> detalles = detalleRepository.findByPabellonId(pabellonId);
        return detalles.stream()
                .map(detalle -> convertirADetalleRespuestaDTO(detalle, pabellonDTO.getNombre()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DetalleEquipamientoRespuestaDTO obtenerDetalleEspecifico(Integer pabellonId, Integer equipamientoId) {
        validarPabellon(pabellonId); // Validamos que el pabellón exista
        DetalleEquipamiento detalle = detalleRepository.findByEquipamientoIdAndPabellonId(equipamientoId, pabellonId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró stock para el equipo ID " + equipamientoId + " en el pabellón ID " + pabellonId));

        PabellonRespuestaDTO pabellonDTO = validarPabellon(pabellonId); // Obtenemos el nombre del pabellón para enriquecer la respuesta
        return convertirADetalleRespuestaDTO(detalle, pabellonDTO.getNombre());
    }

    @Transactional
    public DetalleEquipamientoRespuestaDTO consumirStock(ConsumoStockDTO dto) {
        DetalleEquipamiento detalle = detalleRepository.findById(dto.getDetalleId())
                .orElseThrow(() -> new EntityNotFoundException("Registro de stock no encontrado con ID: " + dto.getDetalleId()));

        int nuevaCantidad = detalle.getCantidad() - dto.getCantidad();
        if (nuevaCantidad < 0) {
            throw new IllegalStateException("No se puede consumir más stock del disponible. Cantidad actual: " + detalle.getCantidad());
        }

        detalle.setCantidad(nuevaCantidad);
        detalle.setFechaActualizacion(LocalDate.now());

        // Lógica de Alerta: si el stock cae por debajo del mínimo
        if (nuevaCantidad < detalle.getStockMinimo()) {
            System.out.println("ALERTA: Stock para '" + detalle.getEquipamiento().getNombre() + "' en pabellón ID " + detalle.getPabellonId() + " ha caído por debajo del mínimo.");
            // Aquí en una aplicación real, se podría enviar una notificación, email, etc.
        }

        DetalleEquipamiento guardado = detalleRepository.save(detalle);
        PabellonRespuestaDTO pabellonDTO = validarPabellon(guardado.getPabellonId());
        return convertirADetalleRespuestaDTO(guardado, pabellonDTO.getNombre());
    }


    // --- Métodos Privados de Ayuda ---

    /**
     * Valida la existencia de un pabellón consultando el servicio de pabellones.
     * Lanza EntityNotFoundException si el pabellón no existe o RuntimeException si hay un error de comunicación.
     */
    private PabellonRespuestaDTO validarPabellon(Integer pabellonId) {
        try {
            return pabellonWebClient.get()
                    .uri("/{id}", pabellonId)
                    .retrieve()
                    .bodyToMono(PabellonRespuestaDTO.class)
                    .block(); // Esperamos la respuesta
        } catch (WebClientResponseException.NotFound ex) {
            throw new EntityNotFoundException("El pabellón con ID " + pabellonId + " no existe en el servicio de pabellones.");
        } catch (Exception ex) {
            throw new RuntimeException("Error de comunicación con el servicio de pabellones: " + ex.getMessage());
        }
    }

    /**
     * Convierte una entidad Equipamiento a un EquipamientoDTO.
     */
    private EquipamientoDTO convertirAEquipamientoDTO(Equipamiento equipamiento) {
        EquipamientoDTO dto = new EquipamientoDTO();
        dto.setId(equipamiento.getId());
        dto.setNombre(equipamiento.getNombre());
        dto.setDescripcion(equipamiento.getDescripcion());
        dto.setEstado(equipamiento.getEstado());
        dto.setFechaAdquisicion(equipamiento.getFechaAdquisicion());
        return dto;
    }

    /**
     * Convierte una entidad DetalleEquipamiento a un DetalleEquipamientoRespuestaDTO,
     * enriqueciendo con el nombre del pabellón.
     */
    private DetalleEquipamientoRespuestaDTO convertirADetalleRespuestaDTO(DetalleEquipamiento detalle, String pabellonNombre) {
        return DetalleEquipamientoRespuestaDTO.builder()
                .id(detalle.getId())
                .cantidad(detalle.getCantidad())
                .stockMinimo(detalle.getStockMinimo())
                .stockMaximo(detalle.getStockMaximo())
                .fechaActualizacion(detalle.getFechaActualizacion())
                .equipamiento(convertirAEquipamientoDTO(detalle.getEquipamiento()))
                .pabellonId(detalle.getPabellonId())
                .pabellonNombre(pabellonNombre) // Añadimos el nombre enriquecido
                .build();
    }
}