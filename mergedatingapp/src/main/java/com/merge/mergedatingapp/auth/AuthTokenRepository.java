package com.merge.mergedatingapp.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthTokenRepository extends JpaRepository<AuthToken, UUID> {

    Optional<AuthToken> findByIdAndRevokedFalse(UUID id);

    void deleteByUserId(UUID userId);
}
