import api from "./client";

export const listThreads = () => api.get("/api/threads");

export const getMessages = (threadId) =>
    api.get(`/api/threads/${threadId}/messages`);

export const sendMessage = (threadId, content) =>
    api.post(`/api/threads/${threadId}/messages`, { content });
