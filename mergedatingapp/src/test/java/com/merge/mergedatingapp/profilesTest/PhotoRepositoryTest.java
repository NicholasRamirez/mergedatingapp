package com.merge.mergedatingapp.profilesTest;

import com.merge.mergedatingapp.profiles.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PhotoRepositoryTest {

    @Autowired
    private PhotoRepository repo;

    @Test
    void findByProfileIdOrderByPositionAsc_returnsSortedPhotos() {
        UUID profileId = UUID.randomUUID();

        Photo p2 = Photo.builder()
                .profileId(profileId)
                .url("u2")
                .position(1)
                .build();

        Photo p1 = Photo.builder()
                .profileId(profileId)
                .url("u1")
                .position(0)
                .build();

        repo.save(p2);
        repo.save(p1);

        List<Photo> result = repo.findByProfileIdOrderByPositionAsc(profileId);

        assertEquals(2, result.size());
        assertEquals("u1", result.get(0).getUrl());
        assertEquals("u2", result.get(1).getUrl());
    }

    @Test
    void countByProfileId_countsCorrectly() {
        UUID profileId = UUID.randomUUID();

        repo.save(Photo.builder().profileId(profileId).url("u1").position(0).build());
        repo.save(Photo.builder().profileId(profileId).url("u2").position(1).build());

        assertEquals(2, repo.countByProfileId(profileId));
    }
}
