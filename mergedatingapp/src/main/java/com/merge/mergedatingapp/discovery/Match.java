package com.merge.mergedatingapp.discovery;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "matches",
        uniqueConstraints = @UniqueConstraint(columnNames = {"userA","userB"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Match {
    @Id @GeneratedValue private UUID id;

    @Column(nullable = false) private UUID userA; // smaller-uuid first
    @Column(nullable = false) private UUID userB;

    @Builder.Default
    @Column(nullable = false) private Instant createdAt = Instant.now();

    @Builder.Default
    @Column(nullable = false) private boolean active = true;
}
