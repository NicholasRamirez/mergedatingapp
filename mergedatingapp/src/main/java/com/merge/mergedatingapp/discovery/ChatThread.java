package com.merge.mergedatingapp.discovery;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

// JPA entity for a chat thread for a specific match.

@Entity @Table(name = "chat_threads", uniqueConstraints = @UniqueConstraint(columnNames = {"matchId"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatThread {

    @Id @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID matchId;

    @Builder.Default
    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Builder.Default
    @Column(nullable = false)
    private boolean archived = false;
}