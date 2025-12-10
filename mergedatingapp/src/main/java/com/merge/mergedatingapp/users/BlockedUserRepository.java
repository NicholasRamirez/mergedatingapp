package com.merge.mergedatingapp.users;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

// Repository for managing BlockedUser relationships.

public interface BlockedUserRepository extends JpaRepository<BlockedUser, UUID> {

    // Returns true if blockerId has already blocked blockedId
    boolean existsByBlockerIdAndBlockedId(UUID blockerId, UUID blockedId);

    // Deletes block records where user is involved (used when deleting accounts)
    void deleteByBlockerIdOrBlockedId(UUID blockerId, UUID blockedId);
}
