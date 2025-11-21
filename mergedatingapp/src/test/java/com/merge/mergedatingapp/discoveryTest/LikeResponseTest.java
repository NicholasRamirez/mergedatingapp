package com.merge.mergedatingapp.discoveryTest;

import com.merge.mergedatingapp.discovery.dto.LikeResponse;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LikeResponseTest {

    @Test
    void liked_factoryMethod_hasStatusLikedAndNullIds() {
        LikeResponse r = LikeResponse.liked();

        assertThat(r.status()).isEqualTo("LIKED");
        assertThat(r.matchId()).isNull();
        assertThat(r.threadId()).isNull();
    }

    @Test
    void matched_factoryMethod_setsStatusAndIds() {
        UUID matchId = UUID.randomUUID();
        UUID threadId = UUID.randomUUID();

        LikeResponse r = LikeResponse.matched(matchId, threadId);

        assertThat(r.status()).isEqualTo("MATCHED");
        assertThat(r.matchId()).isEqualTo(matchId);
        assertThat(r.threadId()).isEqualTo(threadId);
    }
}