import { useEffect, useState } from "react";
import NavBar from "../components/NavBar";
import { getNext, passUser } from "../api/discovery";
import { likeUser } from "../api/matching";
import { useNavigate } from "react-router-dom";
import { getPublicProfile } from "../api/profile";

// Format enums like LONG_TERM → Long Term
function formatEnum(v) {
    return String(v || "")
        .replace(/_/g, " ")
        .toLowerCase()
        .replace(/(^|\s)\S/g, (m) => m.toUpperCase());
}

// Candidate card showing photo, name, city, facts, and prompts
function CandidateCard({ c }) {
    const photo = c?.photos?.[0] || "https://picsum.photos/600/800?grayscale";

    // Support either `prompts: [...]` or `prompts: { items: [...] }`
    const rawPrompts = c?.prompts?.items ?? c?.prompts ?? [];
    const qas = Array.isArray(rawPrompts) ? rawPrompts.slice(0, 3) : [];

    return (
        <div className="bg-white rounded-2xl shadow overflow-hidden max-w-sm mx-auto">
            {/* Candidate photo */}
            <div className="w-full h-[400px] overflow-hidden">
                <img
                    src={photo}
                    alt={c?.name || "candidate"}
                    className="w-full h-full object-cover"
                    loading="lazy"
                />
            </div>

            <div className="p-4">
                {/* Basic identity */}
                <div className="text-xl font-bold">{c?.name ?? "Unknown"}</div>
                <div className="text-gray-600">{c?.city}</div>

                {/* Facts line (gender, pronouns, intent, height) */}
                {(c?.genderType ||
                    c?.pronounsType ||
                    c?.relationshipIntent ||
                    typeof c?.heightCm === "number") && (
                    <div className="mt-2 text-sm text-gray-900">
                        {[
                            c?.genderType && formatEnum(c.genderType),
                            c?.pronounsType && formatEnum(c.pronounsType),
                            c?.relationshipIntent && formatEnum(c.relationshipIntent),
                            typeof c?.heightCm === "number" && `${c.heightCm} cm`,
                        ]
                            .filter(Boolean)
                            .join(" • ")}
                    </div>
                )}

                {/* Prompts */}
                {qas.length > 0 && (
                    <div className="mt-3 grid gap-1">
                        {qas.map((p, i) => (
                            <div
                                key={i}
                                className="border border-gray-200 rounded-lg px-3 py-2"
                            >
                                <div className="font-semibold text-gray-900">{p.question}</div>
                                <div className="text-gray-700">{p.answer || "…"}</div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}

export default function DiscoverPage() {
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState("");
    const [candidate, setCandidate] = useState(null);
    const [likeBusy, setLikeBusy] = useState(false);
    const [passBusy, setPassBusy] = useState(false);
    const [matchInfo, setMatchInfo] = useState(null); // {matchId, threadId}

    const nav = useNavigate();

    // Load next candidate (with public profile info)
    async function loadNext() {
        setErr("");
        setLoading(true);
        setCandidate(null);

        try {
            const { data } = await getNext();
            console.log("DISCOVERY NEXT:", data);

            if (!data) {
                setCandidate(null);
                return;
            }

            // Fetch additional details (gender, pronouns, intent, height)
            let basics = {};
            try {
                const prof = await getPublicProfile(data.userId);
                const p = prof?.data ?? {};
                basics = {
                    genderType: p.gender ?? p.genderType ?? null,
                    pronounsType: p.pronouns ?? p.pronounsType ?? null,
                    relationshipIntent:
                        p.relationshipIntent ?? p.relationshipIntentType ?? null,
                    heightCm: p.heightCm ?? null,
                };
            } catch {
                // ignore errors if public profile not found
            }

            setCandidate({ ...data, ...basics });
        } catch (e) {
            setErr(e?.response?.data?.message || e.message);
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        loadNext();
    }, []);

    async function onPass() {
        if (!candidate) return;
        setPassBusy(true);
        setErr("");
        try {
            await passUser(candidate.userId);
            await loadNext();
        } catch (e) {
            setErr(e?.response?.data?.message || e.message);
        } finally {
            setPassBusy(false);
        }
    }

    async function onLike() {
        if (!candidate) return;
        setLikeBusy(true);
        setErr("");
        try {
            const { data } = await likeUser(candidate.userId);
            if (data?.status === "MATCHED") {
                setMatchInfo({ matchId: data.matchId, threadId: data.threadId });
            }
            await loadNext();
        } catch (e) {
            setErr(e?.response?.data?.message || e.message);
        } finally {
            setLikeBusy(false);
        }
    }

    return (
        <div className="min-h-screen bg-gray-50">
            <NavBar />

            <div className="max-w-sm mx-auto p-4 grid gap-4">
                <h1 className="text-2xl font-bold">Discover</h1>
                {err && <div className="text-red-600">{err}</div>}
                {loading && <div className="text-gray-600">Loading…</div>}

                {!loading && !candidate && (
                    <div className="bg-white rounded-2xl shadow p-6 text-center text-gray-600">
                        No more candidates right now. Check back soon!
                    </div>
                )}

                {candidate && (
                    <>
                        <CandidateCard c={candidate} />

                        {/* Buttons */}
                        <div className="flex justify-center gap-4 mt-2">
                            <button
                                disabled={passBusy || likeBusy}
                                onClick={onPass}
                                className="px-8 py-2 rounded-full border bg-white hover:bg-gray-100 transition"
                            >
                                {passBusy ? "Passing…" : "Pass"}
                            </button>
                            <button
                                disabled={passBusy || likeBusy}
                                onClick={onLike}
                                className="px-8 py-2 rounded-full bg-black text-white hover:bg-gray-900 transition"
                            >
                                {likeBusy ? "Liking…" : "Like"}
                            </button>
                        </div>
                    </>
                )}
            </div>

            {/* Match Banner */}
            {matchInfo && (
                <div className="fixed inset-0 bg-black/40 grid place-items-center p-4">
                    <div className="bg-white rounded-2xl shadow p-6 max-w-sm w-full grid gap-3 text-center">
                        <div className="text-2xl font-bold">It’s a match!</div>
                        <div className="text-gray-600 text-sm">
                            You can start chatting now.
                        </div>
                        <div className="flex gap-3 mt-2">
                            <button
                                className="flex-1 border rounded-xl py-2"
                                onClick={() => setMatchInfo(null)}
                            >
                                Keep browsing
                            </button>
                            <button
                                className="flex-1 bg-black text-white rounded-xl py-2"
                                onClick={() =>
                                    nav("/chat", {
                                        state: {
                                            threadId: matchInfo.threadId,
                                            matchId: matchInfo.matchId,
                                        },
                                    })
                                }
                            >
                                Open chat
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}