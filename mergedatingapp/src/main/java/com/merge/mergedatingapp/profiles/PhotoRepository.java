package com.merge.mergedatingapp.profiles;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PhotoRepository extends JpaRepository<Photo, UUID> {
    List<Photo> findByProfileIdOrderByPositionAsc(UUID profileId);
    long countByProfileId(UUID profileId);

    void deleteByProfileId(UUID profileId);
}
