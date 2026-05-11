package com.digitalsignage.admin.layout.repository;

import com.digitalsignage.admin.entity.Layout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LayoutRepository extends JpaRepository<Layout, Long> {

    List<Layout> findByOrganization_IdOrderByUpdatedAtDesc(Long organizationId);

    Optional<Layout> findByIdAndOrganization_Id(Long id, Long organizationId);
}
