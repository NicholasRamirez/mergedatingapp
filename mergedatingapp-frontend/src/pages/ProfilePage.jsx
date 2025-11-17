import { useEffect, useState } from "react";
import NavBar from "../components/NavBar";
import { getMe, updateBasics, addPhoto, savePrompts } from "../api/profile";

const PRESET_PROMPTS = [
    "Tabs or spaces?",
    "Favorite algorithm?",
    "Best debugging trick?",
    "CS hill you’ll die on?",
    "Most loved language?",
    "What do you build for fun?",
];

export default function ProfilePage() {
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState("");
    const [ok, setOk] = useState("");

    // basics
    const [name, setName] = useState("");
    const [city, setCity] = useState("");
    const [birthday, setBirthday] = useState("");
    const [gender, setGender] = useState("UNSPECIFIED");
    const [pronouns, setPronouns] = useState("UNSPECIFIED");
    const [intent, setIntent] = useState("UNDECIDED");
    const [heightCm, setHeightCm] = useState(170);

    // photos + prompts
    const [photoUrl, setPhotoUrl] = useState("");
    const [prompts, setPrompts] = useState([
        { question: "Tabs or spaces?", answer: "" },
        { question: "Favorite algorithm?", answer: "" },
    ]);

    const [discoverable, setDiscoverable] = useState(false);

    useEffect(() => {
        (async () => {
            try {
                const { data } = await getMe();
                setName(data.name ?? "");
                setCity(data.city ?? "");
                setBirthday(data.birthday ?? "");
                setGender(data.gender ?? "UNSPECIFIED");
                setPronouns(data.pronouns ?? "UNSPECIFIED");
                setIntent(data.relationshipIntent ?? "UNDECIDED");
                setHeightCm(data.heightCm ?? 170);
                if (data.prompts?.items?.length) {
                    // normalize to {question, answer}
                    setPrompts(data.prompts.items.map(p => ({ question: p.question, answer: p.answer || "" })));
                }
                setDiscoverable(Boolean(data.discoverable));
            } catch (e) {
                setErr(e?.response?.data?.message || e.message);
            } finally {
                setLoading(false);
            }
        })();
    }, []);

    async function refreshDiscoverable() {
        try {
            const { data } = await getMe();
            setDiscoverable(Boolean(data.discoverable));
        } catch {}
    }

    async function saveBasics() {
        setErr(""); setOk("");
        try {
            await updateBasics({
                name, city, birthday,
                gender, pronouns,
                relationshipIntent: intent,
                heightCm: Number(heightCm),
            });
            setOk("Basics saved");
            await refreshDiscoverable();
        } catch (e) { setErr(e?.response?.data?.message || e.message); }
    }

    async function addOnePhoto() {
        if (!photoUrl.trim()) return;
        setErr(""); setOk("");
        try {
            await addPhoto(photoUrl.trim(), 0);
            setOk("Photo added");
            setPhotoUrl("");
            await refreshDiscoverable();
        } catch (e) { setErr(e?.response?.data?.message || e.message); }
    }

    async function saveMyPrompts() {
        setErr(""); setOk("");
        try {
            await savePrompts(prompts);
            setOk("Prompts saved");
            await refreshDiscoverable();
        } catch (e) { setErr(e?.response?.data?.message || e.message); }
    }

    const badge =
        discoverable
            ? "bg-green-100 text-green-700"
            : "bg-yellow-100 text-yellow-700";

    if (loading) {
        return (
            <div>
                <NavBar />
                <div className="p-6">Loading…</div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50">
            <NavBar />
            <div className="max-w-3xl mx-auto p-6 grid gap-6">
                <div className="flex items-center gap-3">
                    <h1 className="text-2xl font-bold">My Profile</h1>
                    <span className={`text-sm px-2 py-1 rounded ${badge}`}>
            {discoverable ? "discoverable: true" : "discoverable: false"}
          </span>
                </div>

                {err && <div className="text-red-600">{err}</div>}
                {ok && <div className="text-green-700">{ok}</div>}

                {/* BASICS */}
                <section className="bg-white rounded-2xl shadow p-5 grid gap-3">
                    <div>
                        <h2 className="font-semibold">Basics</h2>
                        <p className="text-xs text-gray-500">Fill these to become discoverable.</p>
                    </div>

                    <div className="grid gap-3">
                        <div className="grid gap-1">
                            <label htmlFor="name" className="text-sm text-gray-600">Name</label>
                            <input id="name" className="border p-2 rounded" value={name} onChange={e=>setName(e.target.value)} />
                        </div>

                        <div className="grid gap-1">
                            <label htmlFor="city" className="text-sm text-gray-600">City</label>
                            <input id="city" className="border p-2 rounded" value={city} onChange={e=>setCity(e.target.value)} />
                        </div>

                        <div className="grid gap-1">
                            <label htmlFor="birthday" className="text-sm text-gray-600">Birthday (YYYY-MM-DD)</label>
                            <input id="birthday" className="border p-2 rounded" placeholder="2000-05-12"
                                   value={birthday} onChange={e=>setBirthday(e.target.value)} />
                        </div>

                        <div className="grid grid-cols-2 gap-3">
                            <div className="grid gap-1">
                                <label htmlFor="gender" className="text-sm text-gray-600">Gender</label>
                                <select id="gender" className="border p-2 rounded" value={gender} onChange={e=>setGender(e.target.value)}>
                                    <option>UNSPECIFIED</option><option>MALE</option><option>FEMALE</option>
                                    <option>NONBINARY</option><option>OTHER</option>
                                </select>
                            </div>

                            <div className="grid gap-1">
                                <label htmlFor="pronouns" className="text-sm text-gray-600">Pronouns</label>
                                <select id="pronouns" className="border p-2 rounded" value={pronouns} onChange={e=>setPronouns(e.target.value)}>
                                    <option>UNSPECIFIED</option><option>HE_HIM</option><option>SHE_HER</option>
                                    <option>THEY_THEM</option><option>OTHER</option>
                                </select>
                            </div>

                            <div className="grid gap-1">
                                <label htmlFor="intent" className="text-sm text-gray-600">Relationship intent</label>
                                <select id="intent" className="border p-2 rounded" value={intent} onChange={e=>setIntent(e.target.value)}>
                                    <option>UNDECIDED</option><option>LONG_TERM</option><option>SHORT_TERM</option>
                                    <option>FRIENDSHIP</option><option>CASUAL</option>
                                </select>
                            </div>

                            <div className="grid gap-1">
                                <label htmlFor="height" className="text-sm text-gray-600">Height (cm)</label>
                                <input id="height" type="number" className="border p-2 rounded"
                                       value={heightCm} onChange={e=>setHeightCm(e.target.value)} />
                            </div>
                        </div>

                        <button className="bg-black text-white rounded px-4 py-2 w-fit" onClick={saveBasics}>
                            Save basics
                        </button>
                    </div>
                </section>

                {/* PHOTO */}
                <section className="bg-white rounded-2xl shadow p-5 grid gap-3">
                    <h2 className="font-semibold">Photo</h2>
                    <div className="grid gap-1">
                        <label htmlFor="photoUrl" className="text-sm text-gray-600">Photo URL</label>
                        <input id="photoUrl" className="border p-2 rounded" placeholder="https://…"
                               value={photoUrl} onChange={e=>setPhotoUrl(e.target.value)} />
                    </div>
                    <button className="border rounded px-4 py-2 w-fit" onClick={addOnePhoto}>Add photo</button>
                </section>

                {/* PROMPTS */}
                <section className="bg-white rounded-2xl shadow p-5 grid gap-3">
                    <h2 className="font-semibold">Prompts</h2>

                    {prompts.map((p, i) => (
                        <div key={i} className="grid gap-2 bg-gray-50 rounded-xl p-3">
                            <div className="grid gap-1">
                                <label className="text-sm text-gray-600">Prompt {i + 1}</label>
                                <select
                                    className="border p-2 rounded"
                                    value={p.question || ""}
                                    onChange={e => {
                                        const copy = [...prompts];
                                        copy[i] = { ...p, question: e.target.value };
                                        setPrompts(copy);
                                    }}
                                >
                                    <option value="">{p.question ? `(Keep) ${p.question}` : "Choose a preset…"}</option>
                                    {PRESET_PROMPTS.map(q => <option key={q} value={q}>{q}</option>)}
                                </select>
                            </div>

                            <div className="grid gap-1">
                                <label className="text-sm text-gray-600">Your answer</label>
                                <input
                                    className="border p-2 rounded"
                                    value={p.answer}
                                    onChange={e => {
                                        const copy = [...prompts];
                                        copy[i] = { ...p, answer: e.target.value };
                                        setPrompts(copy);
                                    }}
                                />
                            </div>
                        </div>
                    ))}

                    <div className="flex gap-3">
                        <button
                            className="border rounded px-4 py-2"
                            onClick={() => setPrompts(prev => [...prev, { question: "", answer: "" }])}
                        >
                            + Add another prompt
                        </button>
                        <button className="bg-black text-white rounded px-4 py-2" onClick={saveMyPrompts}>
                            Save prompts
                        </button>
                    </div>
                </section>
            </div>
        </div>
    );
}