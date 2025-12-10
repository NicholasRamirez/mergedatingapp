package com.merge.mergedatingapp.discovery;

import com.merge.mergedatingapp.profiles.Profile;

import java.util.List;
import java.util.UUID;

//Strategy interface for ordering candidate profiles in discovery feed.

public interface CandidateOrderingStrategy {

     // Order the given list of discoverable profiles for this viewer.
    List<Profile> orderCandidates(List<Profile> candidates, UUID viewerUserId);
}
