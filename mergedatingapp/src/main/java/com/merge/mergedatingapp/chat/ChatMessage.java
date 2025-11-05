package com.merge.mergedatingapp.chat;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "chat_messages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatMessage {
    @Id @GeneratedValue private UUID id;

    @Column(nullable = false) private UUID threadId;
    @Column(nullable = false) private UUID senderId;

    @Builder.Default
    @Column(nullable = false) private Instant sentAt = Instant.now();

    @Column(nullable = false, length = 2048) private String content;
}
