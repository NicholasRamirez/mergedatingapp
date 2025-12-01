import api, { setToken } from "./client";

export function getMe() {
    return api.get("/api/auth/user");
}

export async function logout() {
    try {
        await api.post("/api/auth/logout");
    } catch (e) {
        // If the token is already invalid / expired, we still clear it on client
        console.error("Logout failed (ignoring):", e);
    } finally {
        setToken(null); // clears Authorization + localStorage
    }
}