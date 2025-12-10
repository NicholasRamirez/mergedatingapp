package com.merge.mergedatingapp.users;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

// Core user entity for authentication and ownership

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    private Instant createdAt = Instant.now();
}