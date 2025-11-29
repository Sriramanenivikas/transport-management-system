package com.tms.repository;

import com.tms.entity.TruckCapacity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TruckCapacityRepository extends JpaRepository<TruckCapacity, UUID> {
}
