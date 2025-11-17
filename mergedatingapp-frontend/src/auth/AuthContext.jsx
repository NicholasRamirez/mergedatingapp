import { createContext, useContext, useEffect, useState } from "react";
import api, { setToken, getToken } from "../api/client";

const AuthCtx = createContext(null);
export function useAuth(){ return useContext(AuthCtx); }

export default function AuthProvider({ children }) {
    const [token, setTok] = useState(getToken());
    const [user, setUser] = useState(null);

    useEffect(() => {
        if (token) {
            setToken(token);
            api.get("/api/auth/user").then(r => setUser(r.data)).catch(() => setUser(null));
        } else {
            setUser(null);
        }
    }, [token]);

    const loginOk = t => setTok(t);
    const logout = () => setTok("");

    return (
        <AuthCtx.Provider value={{ token, user, loginOk, logout }}>
            {children}
        </AuthCtx.Provider>
    );
}