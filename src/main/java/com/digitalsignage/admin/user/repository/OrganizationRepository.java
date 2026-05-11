package com.digitalsignage.admin.user.repository;

import com.digitalsignage.admin.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
}
