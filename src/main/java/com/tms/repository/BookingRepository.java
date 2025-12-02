package com.tms.repository;

import com.tms.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    List<Booking> findByLoadId(Integer loadId);

    List<Booking> findByTransporterId(Integer transporterId);

    @Query("SELECT COALESCE(SUM(b.allocatedTrucks), 0) FROM Booking b WHERE b.loadId = :loadId AND b.status != 'CANCELLED'")
    Integer getTotalAllocatedTrucks(Integer loadId);
}

