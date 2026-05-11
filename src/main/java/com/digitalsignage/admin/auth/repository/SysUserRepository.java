package com.digitalsignage.admin.auth.repository;

import com.digitalsignage.admin.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SysUserRepository extends JpaRepository<SysUser, Long> {

    Optional<SysUser> findByUsername(String username);

    @Query("SELECT u FROM SysUser u JOIN FETCH u.organization WHERE u.id = :id")
    Optional<SysUser> findByIdWithOrganization(@Param("id") Long id);

    List<SysUser> findByOrganization_IdOrderByUsernameAsc(Long organizationId);

    Optional<SysUser> findByIdAndOrganization_Id(Long id, Long organizationId);

    boolean existsByOrganization_IdAndUsername(Long organizationId, String username);
}
