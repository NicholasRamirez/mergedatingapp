import { BrowserRouter, Routes, Route } from "react-router-dom";
import AuthProvider from "./auth/AuthContext";
import RequireAuth from "./auth/RequireAuth";
import LoginPage from "./pages/LoginPage";
import ProfilePage from "./pages/ProfilePage";
import DiscoverPage from "./pages/DiscoverPage";
import ChatPage from "./pages/ChatPage";

export default function App(){
    return (
        <AuthProvider>
            <BrowserRouter>
                <Routes>
                    <Route path="/login" element={<LoginPage/>} />
                    <Route path="/profile" element={<RequireAuth><ProfilePage/></RequireAuth>} />
                    <Route path="/discover" element={<RequireAuth><DiscoverPage/></RequireAuth>} />
                    <Route path="/chat" element={<RequireAuth><ChatPage/></RequireAuth>} />
                    <Route path="*" element={<LoginPage/>} />
                </Routes>
            </BrowserRouter>
        </AuthProvider>
    );
}
