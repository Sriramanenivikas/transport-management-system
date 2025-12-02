package com.tms.repository;

import com.tms.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidRepository extends JpaRepository<Bid, Integer> {

    List<Bid> findByLoadId(Integer loadId);

    List<Bid> findByTransporterId(Integer transporterId);

    List<Bid> findByStatus(String status);

    List<Bid> findByLoadIdAndStatus(Integer loadId, String status);

    List<Bid> findByTransporterIdAndStatus(Integer transporterId, String status);

    @Query("SELECT SUM(b.trucksOffered) FROM Bid b WHERE b.loadId = :loadId AND b.status = 'ACCEPTED'")
    Integer getTotalAllocatedTrucks(@Param("loadId") Integer loadId);
}

