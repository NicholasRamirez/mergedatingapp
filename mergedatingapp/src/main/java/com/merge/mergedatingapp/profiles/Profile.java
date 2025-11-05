package com.merge.mergedatingapp.profiles;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static com.merge.mergedatingapp.profiles.Enums.*;

@Entity @Table(name = "profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Profile {
    @Id @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID userId; // 1–1 with User

    private String name;
    private LocalDate birthday;

    @Enumerated(EnumType.STRING)
    private GenderType gender = GenderType.UNSPECIFIED;

    @Enumerated(EnumType.STRING)
    private PronounsType pronouns = PronounsType.UNSPECIFIED;

    @Enumerated(EnumType.STRING)
    private RelationshipIntentType relationshipIntent = RelationshipIntentType.UNDECIDED;

    private Integer heightCm;
    private String city;

    @Column(nullable = false)
    private boolean discoverable = false;

    @Column(nullable = false)
    private Instant lastUpdated = Instant.now();
}
