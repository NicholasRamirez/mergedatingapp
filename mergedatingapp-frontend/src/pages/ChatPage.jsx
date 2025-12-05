import { useEffect, useState } from "react";
import NavBar from "../components/NavBar";
import {
    listThreads,
    getMessages,
    sendMessage as apiSendMessage,
} from "../api/chat";
import { blockUser } from "../api/account";

export default function ChatPage() {
    const [threads, setThreads] = useState([]);
    const [activeThread, setActiveThread] = useState(null);
    const [messages, setMessages] = useState([]);
    const [typed, setTyped] = useState("");
    const [loadingThreads, setLoadingThreads] = useState(true);
    const [sending, setSending] = useState(false);
    const [blockError, setBlockError] = useState("");

    // Load all threads on mount
    useEffect(() => {
        async function load() {
            setLoadingThreads(true);
            try {
                const { data } = await listThreads();
                const arr = data || [];
                setThreads(arr);
                if (arr.length > 0) {
                    setActiveThread(arr[0]);
                }
            } catch (e) {
                console.error("listThreads failed", e);
                setThreads([]);
            } finally {
                setLoadingThreads(false);
            }
        }
        load();
    }, []);

    // Load messages whenever the active thread changes
    useEffect(() => {
        if (!activeThread) {
            setMessages([]);
            return;
        }

        async function loadMessages() {
            try {
                const { data } = await getMessages(activeThread.threadId);
                setMessages(data || []);
            } catch (e) {
                console.error("getMessages failed", e);
                setMessages([]);
            }
        }

        loadMessages();
    }, [activeThread]);

    async function handleSend(e) {
        e.preventDefault();
        if (!typed.trim() || !activeThread) return;

        setSending(true);
        try {
            const { data } = await apiSendMessage(
                activeThread.threadId,
                typed.trim()
            );
            setMessages((prev) => [...prev, data]);
            setTyped("");
        } catch (e) {
            console.error("sendMessage failed", e);
        } finally {
            setSending(false);
        }
    }

    // Block the current conversation partner
    async function handleBlockUser() {
        if (!activeThread) return;

        const ok = window.confirm(
            `Block ${activeThread.partnerName || "this user"}?\n` +
            "You will no longer see them in Discover."
        );
        if (!ok) return;

        setBlockError("");

        try {
            // Call backend: /api/account/block
            await blockUser(activeThread.partnerUserId);

            // Locally remove this thread from list & clear messages
            setThreads((prev) =>
                prev.filter((t) => t.threadId !== activeThread.threadId)
            );
            setActiveThread(null);
            setMessages([]);
        } catch (e) {
            console.error("blockUser failed", e);
            setBlockError(
                e?.response?.data?.message || "Failed to block user. Please try again."
            );
        }
    }

    // Label + side for bubbles
    function isMine(message, thread) {
        if (!thread) return false;
        // If senderId equals partnerUserId, it's "Them", otherwise "You"
        return message.senderId !== thread.partnerUserId;
    }

    function labelFor(message, thread) {
        return isMine(message, thread) ? "You" : "Them";
    }

    const activeName = activeThread?.partnerName || "Match";

    return (
        <div className="min-h-screen bg-gray-50">
            <NavBar />

            <div className="max-w-5xl mx-auto mt-4 h-[calc(100vh-5rem)] bg-white shadow rounded-2xl overflow-hidden flex border border-gray-200">
                {/* LEFT COLUMN – thread list */}
                <aside className="w-64 border-r border-gray-200 bg-gray-50 flex flex-col">
                    <h1 className="text-2xl font-bold px-4 py-3 border-b border-gray-200">
                        Chats
                    </h1>

                    {loadingThreads && (
                        <div className="px-4 py-3 text-sm text-gray-500">Loading…</div>
                    )}

                    {!loadingThreads && threads.length === 0 && (
                        <div className="px-4 py-3 text-sm text-gray-600">
                            You don&apos;t have any matches yet.{" "}
                            <span className="font-semibold">
                Go like some people on Discover!
              </span>
                        </div>
                    )}

                    <ul className="flex-1 overflow-y-auto py-2">
                        {threads.map((t) => {
                            const isActive = activeThread?.threadId === t.threadId;
                            const name = t.partnerName || "Match";
                            return (
                                <li key={t.threadId}>
                                    <button
                                        type="button"
                                        onClick={() => setActiveThread(t)}
                                        className={
                                            "w-full text-left px-4 py-2 text-sm flex items-center justify-between " +
                                            (isActive ? "bg-white font-semibold" : "hover:bg-gray-100")
                                        }
                                    >
                                        <span className="truncate">{name}</span>
                                    </button>
                                </li>
                            );
                        })}
                    </ul>
                </aside>

                {/* RIGHT COLUMN – conversation */}
                <main className="flex-1 flex flex-col">
                    {!activeThread ? (
                        <div className="m-6 text-center text-gray-500">
                            Select a conversation on the left.
                        </div>
                    ) : (
                        <>
                            {/* Header – name + block button */}
                            <div className="px-6 py-3 border-b border-gray-200 flex items-center justify-between">
                                <div>
                                    <div className="text-lg font-semibold">{activeName}</div>
                                    <div className="text-xs text-gray-500" />
                                </div>

                                {/* Block user button */}
                                <button
                                    type="button"
                                    onClick={handleBlockUser}
                                    className="text-xs px-3 py-1 rounded-full border border-red-500 text-red-600 hover:bg-red-50"
                                >
                                    Block user
                                </button>
                            </div>

                            {/* Show block error if any */}
                            {blockError && (
                                <div className="px-6 py-2 text-xs text-red-600 border-b border-red-100 bg-red-50">
                                    {blockError}
                                </div>
                            )}

                            {/* Messages */}
                            <div className="flex-1 overflow-y-auto px-6 py-4 space-y-3 bg-gray-50">
                                {messages.length === 0 ? (
                                    <div className="text-gray-500 text-sm text-center mt-8">
                                        No messages yet. Start the conversation!
                                    </div>
                                ) : (
                                    messages.map((m) => {
                                        const mine = isMine(m, activeThread);
                                        return (
                                            <div
                                                key={m.id}
                                                className={
                                                    "flex " + (mine ? "justify-end" : "justify-start")
                                                }
                                            >
                                                <div
                                                    className={
                                                        "max-w-xs rounded-2xl px-3 py-2 text-sm shadow-sm " +
                                                        (mine
                                                            ? "bg-black text-white rounded-br-none"
                                                            : "bg-white text-gray-900 rounded-bl-none")
                                                    }
                                                >
                                                    <div className="text-[10px] uppercase tracking-wide mb-1 opacity-70">
                                                        {labelFor(m, activeThread)}
                                                    </div>
                                                    <div>{m.content}</div>
                                                </div>
                                            </div>
                                        );
                                    })
                                )}
                            </div>

                            {/* Input box */}
                            <form
                                onSubmit={handleSend}
                                className="border-t border-gray-200 flex items-center gap-2 px-4 py-3 bg-white"
                            >
                                <input
                                    type="text"
                                    value={typed}
                                    onChange={(e) => setTyped(e.target.value)}
                                    placeholder="Type a message…"
                                    className="flex-1 px-3 py-2 text-sm border border-gray-300 rounded-full focus:outline-none focus:ring-2 focus:ring-black focus:border-transparent"
                                />
                                <button
                                    type="submit"
                                    disabled={sending || !typed.trim()}
                                    className="px-4 py-2 text-sm rounded-full bg-black text-white disabled:opacity-40 disabled:cursor-not-allowed"
                                >
                                    Send
                                </button>
                            </form>
                        </>
                    )}
                </main>
            </div>
        </div>
    );
}