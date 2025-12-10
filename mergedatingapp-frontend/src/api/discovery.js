import api from "./client";

// { userId, name, city, photos[]}
export const getNext = () => api.get("/api/discovery/next");

export const passUser = (candidateUserId) => api.post("/api/discovery/pass", { candidateUserId });