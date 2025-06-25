package com.clinicanuevomilenio.equipamientos.repository;

import com.clinicanuevomilenio.equipamientos.models.DetalleEquipamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DetalleEquipamientoRepository extends JpaRepository<DetalleEquipamiento, Integer> {

    List<DetalleEquipamiento> findByPabellonId(Integer pabellonId);

    // --- ASEGÚRATE DE TENER ESTE MÉTODO ---
    Optional<DetalleEquipamiento> findByEquipamientoIdAndPabellonId(Integer equipamientoId, Integer pabellonId);
}