package com.tms.repository;

import com.tms.entity.Bid;
import com.tms.entity.BidStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BidRepository extends JpaRepository<Bid, UUID> {

    List<Bid> findByLoadLoadId(UUID loadId);

    List<Bid> findByTransporterTransporterId(UUID transporterId);

    List<Bid> findByStatus(BidStatus status);

    List<Bid> findByLoadLoadIdAndStatus(UUID loadId, BidStatus status);

    List<Bid> findByLoadLoadIdAndTransporterTransporterId(UUID loadId, UUID transporterId);

    List<Bid> findByTransporterTransporterIdAndStatus(UUID transporterId, BidStatus status);

    @Query("SELECT b FROM Bid b WHERE b.load.loadId = :loadId AND b.status = 'PENDING' ORDER BY b.proposedRate ASC")
    List<Bid> findPendingBidsByLoadIdSortedByRate(@Param("loadId") UUID loadId);

    @Query("SELECT b FROM Bid b JOIN FETCH b.transporter WHERE b.load.loadId = :loadId AND b.status = 'PENDING'")
    List<Bid> findPendingBidsWithTransporterByLoadId(@Param("loadId") UUID loadId);

    Optional<Bid> findByLoadLoadIdAndTransporterTransporterIdAndStatus(UUID loadId, UUID transporterId, BidStatus status);
}
