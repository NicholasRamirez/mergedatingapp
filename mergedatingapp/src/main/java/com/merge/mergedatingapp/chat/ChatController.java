package com.merge.mergedatingapp.chat;

import com.merge.mergedatingapp.auth.AuthService;
import com.merge.mergedatingapp.auth.dto.userResponse;
import com.merge.mergedatingapp.chat.dto.MessageRequest;
import com.merge.mergedatingapp.chat.dto.MessageResponse;
import com.merge.mergedatingapp.chat.dto.ThreadSummary;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatController {

    private final AuthService auth;
    private final ChatService svc;

    private UUID uid(String header) {
        userResponse user = auth.getUserDevToken(header);
        return user.userId();
    }

    @GetMapping("/threads")
    public List<ThreadSummary> myThreads(@RequestHeader("Authorization") String h) {
        return svc.listThreads(uid(h));
    }

    @GetMapping("/threads/{threadId}/messages")
    public List<MessageResponse> getMessages(@RequestHeader("Authorization") String h,
                                             @PathVariable UUID threadId) {
        return svc.getMessages(uid(h), threadId);
    }

    @PostMapping("/threads/{threadId}/messages")
    public MessageResponse send(@RequestHeader("Authorization") String h,
                                @PathVariable UUID threadId,
                                @Valid @RequestBody MessageRequest req) {
        return svc.send(uid(h), threadId, req);
    }
}
