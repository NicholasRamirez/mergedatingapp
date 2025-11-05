package com.merge.mergedatingapp.discovery;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"likerId","likedId"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LikeEntity {
    @Id @GeneratedValue private UUID id;

    @Column(nullable = false) private UUID likerId;
    @Column(nullable = false) private UUID likedId;

    @Builder.Default
    @Column(nullable = false) private Instant createdAt = Instant.now();
}