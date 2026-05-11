package com.digitalsignage.admin.layout.repository;

import com.digitalsignage.admin.entity.LayoutRegion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LayoutRegionRepository extends JpaRepository<LayoutRegion, Long> {

    @Query("SELECT r FROM LayoutRegion r WHERE r.layout.id = :layoutId ORDER BY r.zIndex ASC, r.id ASC")
    List<LayoutRegion> findByLayoutIdOrderBySort(@Param("layoutId") Long layoutId);

    void deleteByLayout_Id(Long layoutId);
}
