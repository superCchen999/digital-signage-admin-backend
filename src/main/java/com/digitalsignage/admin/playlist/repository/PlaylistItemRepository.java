package com.digitalsignage.admin.playlist.repository;

import com.digitalsignage.admin.entity.PlaylistItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistItemRepository extends JpaRepository<PlaylistItem, Long> {

    boolean existsByMedia_Id(Long mediaId);
}
