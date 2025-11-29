package com.tms.repository;

import com.tms.entity.Load;
import com.tms.entity.LoadStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoadRepository extends JpaRepository<Load, UUID> {

    Page<Load> findByShipperId(String shipperId, Pageable pageable);

    Page<Load> findByStatus(LoadStatus status, Pageable pageable);

    Page<Load> findByShipperIdAndStatus(String shipperId, LoadStatus status, Pageable pageable);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT l FROM Load l WHERE l.loadId = :loadId")
    Optional<Load> findByIdWithLock(@Param("loadId") UUID loadId);
}
