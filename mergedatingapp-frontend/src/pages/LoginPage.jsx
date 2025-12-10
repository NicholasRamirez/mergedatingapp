import { useState } from "react";
import api from "../api/client";
import { useAuth } from "../auth/AuthContext";
import { useNavigate } from "react-router-dom";

export default function LoginPage() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [mode, setMode] = useState("login"); // "login" | "register"
    const [err, setErr] = useState("");
    const { loginOk } = useAuth();
    const nav = useNavigate();

    async function submit(e) {
        e.preventDefault();
        setErr("");

        try {
            if (mode === "register") {
                await api.post("/api/auth/register", { username, password });
            }
            const { data } = await api.post("/api/auth/login", { username, password });
            loginOk(data.accessToken);
            nav("/profile");
        } catch (ex) {
            setErr(ex?.response?.data?.message || ex.message);
        }
    }

    return (
        <div className="min-h-screen grid place-items-center bg-gray-50">
            <form
                onSubmit={submit}
                className="w-[360px] bg-white p-6 rounded-2xl shadow-md flex flex-col gap-4"
            >
                <h1 className="text-2xl font-semibold tracking-tight">
                    Merge — {mode === "login" ? "Sign in" : "Create account"}
                </h1>

                {err && (
                    <div className="text-red-600 text-sm bg-red-50 border border-red-200 rounded p-2">
                        {err}
                    </div>
                )}

                <input
                    className="border rounded px-3 py-2 w-full focus:outline-none focus:ring-2 focus:ring-gray-300"
                    placeholder="Username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                />

                <input
                    className="border rounded px-3 py-2 w-full focus:outline-none focus:ring-2 focus:ring-gray-300"
                    placeholder="Password"
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                />

                <button className="bg-black text-white rounded py-2 font-medium hover:bg-gray-800 transition">
                    {mode === "login" ? "Sign in" : "Register"}
                </button>

                <button
                    type="button"
                    className="text-sm text-gray-600 underline hover:text-black transition"
                    onClick={() =>
                        setMode(mode === "login" ? "register" : "login")
                    }
                >
                    {mode === "login"
                        ? "Create an account"
                        : "Have an account? Sign in"}
                </button>
            </form>
        </div>
    );
}