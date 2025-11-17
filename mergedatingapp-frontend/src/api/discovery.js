import api from "./client";

export const getNext = () => api.get("/api/discovery/next");            // -> { userId, name, city, photos[], summary? }
export const passUser = (candidateUserId) => api.post("/api/discovery/pass", { candidateUserId });