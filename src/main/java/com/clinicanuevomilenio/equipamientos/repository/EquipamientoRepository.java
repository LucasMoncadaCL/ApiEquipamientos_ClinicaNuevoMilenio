package com.clinicanuevomilenio.equipamientos.repository;

import com.clinicanuevomilenio.equipamientos.models.Equipamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipamientoRepository extends JpaRepository<Equipamiento, Integer> {
}