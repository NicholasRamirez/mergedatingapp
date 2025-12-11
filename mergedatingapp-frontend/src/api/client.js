import axios from "axios";

const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE || "http://localhost:8080",
    withCredentials: true,
});

const saved = localStorage.getItem("token");
if (saved) {
    api.defaults.headers.common["Authorization"] = saved;
}

export function setToken(token) {
    if (token) {
        const value =
            token.startsWith("Bearer ") || token.startsWith("Dev ")
                ? token
                : `Bearer ${token}`;

        localStorage.setItem("token", value);
        api.defaults.headers.common["Authorization"] = value;
    } else {
        localStorage.removeItem("token");
        delete api.defaults.headers.common["Authorization"];
    }
}

export function getToken() {
    return localStorage.getItem("token") || "";
}

export default api;