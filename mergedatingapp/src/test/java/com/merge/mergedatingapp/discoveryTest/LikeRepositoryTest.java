package com.merge.mergedatingapp.discoveryTest;

import com.merge.mergedatingapp.discovery.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class LikeRepositoryTest {

    @Autowired LikeRepository repo;

    @Test
    void existsByLikerAndLiked_works() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        repo.save(LikeEntity.builder().likerId(a).likedId(b).build());

        assertThat(repo.existsByLikerIdAndLikedId(a, b)).isTrue();
        assertThat(repo.existsByLikerIdAndLikedId(b, a)).isFalse();
    }
}