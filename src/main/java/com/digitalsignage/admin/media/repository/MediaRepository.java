package com.digitalsignage.admin.media.repository;

import com.digitalsignage.admin.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MediaRepository extends JpaRepository<Media, Long> {

    @Query("SELECT m FROM Media m JOIN FETCH m.organization WHERE m.organization.id = :orgId "
            + "ORDER BY m.createdAt DESC")
    List<Media> findAllByOrganizationId(@Param("orgId") Long orgId);

    @Query("SELECT m FROM Media m JOIN FETCH m.organization WHERE m.id = :id AND m.organization.id = :orgId")
    Optional<Media> findByIdAndOrganizationId(@Param("id") Long id, @Param("orgId") Long orgId);

    boolean existsByOrganization_IdAndObjectKey(Long organizationId, String objectKey);
}
