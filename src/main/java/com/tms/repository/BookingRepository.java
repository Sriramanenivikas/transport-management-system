package com.tms.repository;

import com.tms.entity.Booking;
import com.tms.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByLoadLoadId(UUID loadId);

    List<Booking> findByTransporterTransporterId(UUID transporterId);

    List<Booking> findByStatus(BookingStatus status);

    @Query("SELECT SUM(b.allocatedTrucks) FROM Booking b WHERE b.load.loadId = :loadId AND b.status != 'CANCELLED'")
    Integer sumAllocatedTrucksByLoadId(@Param("loadId") UUID loadId);

    @Query("SELECT b FROM Booking b WHERE b.load.loadId = :loadId AND b.status != 'CANCELLED'")
    List<Booking> findActiveBookingsByLoadId(@Param("loadId") UUID loadId);
}
