package com.merge.mergedatingapp.profiles;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

// Photo entity for user's profile

@Entity @Table(name = "photos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Photo {

    @Id @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID profileId;

    @Column(nullable = false, length = 2048)
    private String url;

    @Column(nullable = false)
    private int position; // 0,1,2...
}
