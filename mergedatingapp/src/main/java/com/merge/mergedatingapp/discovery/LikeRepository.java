package com.merge.mergedatingapp.discovery;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

// Repository for LikeEntity to check for mutual likes.

public interface LikeRepository extends JpaRepository<LikeEntity, UUID> {

    boolean existsByLikerIdAndLikedId(UUID a, UUID b);

    Optional<LikeEntity> findByLikerIdAndLikedId(UUID a, UUID b);

    void deleteByLikerId(UUID likerId);

    void deleteByLikedId(UUID likedId);
}
