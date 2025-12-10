package com.merge.mergedatingapp.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    List<ChatMessage> findByThreadIdOrderBySentAtAsc(UUID threadId);

    void deleteByThreadId(UUID threadId);
}
