import { useState } from "react";
import api from "../api/client";
import { useAuth } from "../auth/AuthContext";
import { useNavigate } from "react-router-dom";

export default function LoginPage(){
    const [email,setEmail]=useState("");
    const [password,setPassword]=useState("");
    const [mode,setMode]=useState("login"); // "login" | "register"
    const [err,setErr]=useState("");
    const { loginOk } = useAuth();
    const nav = useNavigate();

    async function submit(e){
        e.preventDefault(); setErr("");
        try{
            if(mode==="register") await api.post("/api/auth/register",{email,password});
            const { data } = await api.post("/api/auth/login",{email,password});
            loginOk(data.accessToken); // token = "dev-<uuid>"
            nav("/profile");
        }catch(ex){
            setErr(ex?.response?.data?.message || ex.message);
        }
    }

    return (
        <div className="min-h-screen grid place-items-center bg-gray-50">
            <form onSubmit={submit} className="w-full max-w-sm bg-white p-6 rounded-2xl shadow grid gap-3">
                <h1 className="text-2xl font-bold">Merge — {mode==="login"?"Sign in":"Create account"}</h1>
                {err && <div className="text-red-600 text-sm">{err}</div>}
                <input className="border p-2 rounded" placeholder="email" value={email} onChange={e=>setEmail(e.target.value)} />
                <input className="border p-2 rounded" placeholder="password" type="password" value={password} onChange={e=>setPassword(e.target.value)} />
                <button className="bg-black text-white rounded py-2">{mode==="login"?"Sign in":"Register"}</button>
                <button type="button" className="text-sm underline" onClick={()=>setMode(mode==="login"?"register":"login")}>
                    {mode==="login"?"Create an account":"Have an account? Sign in"}
                </button>
            </form>
        </div>
    );
}