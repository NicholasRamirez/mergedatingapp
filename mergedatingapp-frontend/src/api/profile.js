import api from "./client";

export const getMe = () => api.get("/api/profile/user");
export const updateBasics = (body) => api.put("/api/profile/user", body);
export const addPhoto = (url, position=0) => api.post("/api/profile/user/photos", { url, position });
export const savePrompts = (items) => api.put("/api/profile/user/prompts", items);
export const getPublicProfile = (userId) => api.get(`/api/profile/public/${userId}`);