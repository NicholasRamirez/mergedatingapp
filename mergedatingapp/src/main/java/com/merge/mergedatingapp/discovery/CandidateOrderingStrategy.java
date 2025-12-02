package com.merge.mergedatingapp.discovery;

import com.merge.mergedatingapp.profiles.Profile;

import java.util.List;
import java.util.UUID;


//Strategy for ordering candidate profiles for discovery.

public interface CandidateOrderingStrategy {

    /**
     * Order the given list of discoverable profiles for this viewer.
     *
     * @param candidates discoverable profiles (not including the viewer)
     * @param viewerUserId id of the current viewer
     * @return ordered list of candidates
     */
    List<Profile> orderCandidates(List<Profile> candidates, UUID viewerUserId);
}
