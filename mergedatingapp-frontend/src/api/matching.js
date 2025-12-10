import api from "./client";

// { status: "LIKED" | "MATCHED"}
export const likeUser = (likedUserId) => api.post("/api/matching/like", { likedUserId });