package com.merge.mergedatingapp.discovery;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "seen_candidates",
        uniqueConstraints = @UniqueConstraint(columnNames = {"viewerUserId","candidateUserId"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeenCandidate {
    @Id @GeneratedValue private UUID id;

    @Column(nullable = false) private UUID viewerUserId;
    @Column(nullable = false) private UUID candidateUserId;

    @Builder.Default
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Decision decision = Decision.NONE;

    @Builder.Default
    @Column(nullable = false) private Instant seenAt = Instant.now();

    public enum Decision { NONE, PASS, LIKE }
}