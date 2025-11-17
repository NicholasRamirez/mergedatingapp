import api from "./client";

export const likeUser = (likedUserId) => api.post("/api/matching/like", { likedUserId });
// -> { status: "LIKED" | "MATCHED", matchId:?, threadId:? }