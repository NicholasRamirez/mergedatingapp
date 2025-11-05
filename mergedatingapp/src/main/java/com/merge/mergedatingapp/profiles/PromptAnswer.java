package com.merge.mergedatingapp.profiles;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity @Table(name = "prompt_answers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PromptAnswer {
    @Id @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID profileId;

    @Column(nullable = false)
    private String question;

    @Column(nullable = false, length = 2048)
    private String answer;
}
