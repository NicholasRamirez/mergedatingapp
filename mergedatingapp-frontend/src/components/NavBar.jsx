import { Link } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export default function NavBar(){
    const { logout } = useAuth();
    return (
        <div className="flex items-center gap-4 p-3 border-b bg-white">
            <div className="font-bold">Merge</div>
            <Link to="/profile">Profile</Link>
            <Link to="/discover">Discover</Link>
            <Link to="/chat">Chat</Link>
            <button onClick={logout} className="ml-auto border px-3 py-1 rounded">Logout</button>
        </div>
    );
}