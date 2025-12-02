package com.tms.repository;

import com.tms.entity.TruckCapacity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TruckCapacityRepository extends JpaRepository<TruckCapacity, Integer> {

    Optional<TruckCapacity> findByTransporterTransporterIdAndTruckType(Integer transporterId, String truckType);

    @Modifying
    @Query("DELETE FROM TruckCapacity tc WHERE tc.transporter.transporterId = :transporterId")
    void deleteByTransporterTransporterId(Integer transporterId);
}

