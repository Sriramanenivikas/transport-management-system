package com.tms.repository;

import com.tms.entity.Load;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LoadRepository extends JpaRepository<Load, Integer> {

    Page<Load> findByShipperId(String shipperId, Pageable pageable);

    Page<Load> findByStatus(String status, Pageable pageable);

    Page<Load> findByShipperIdAndStatus(String shipperId, String status, Pageable pageable);
}

