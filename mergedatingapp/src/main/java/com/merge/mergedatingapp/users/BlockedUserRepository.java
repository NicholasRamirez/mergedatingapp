package com.merge.mergedatingapp.users;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BlockedUserRepository extends JpaRepository<BlockedUser, UUID> {

    boolean existsByBlockerIdAndBlockedId(UUID blockerId, UUID blockedId);

    void deleteByBlockerIdOrBlockedId(UUID blockerId, UUID blockedId);
}
