// src/api/account.js
import api from "./client";

// DELETE /api/account  (delete *my* account)
export function deleteMyAccount() {
    return api.delete("/api/account");
}

// POST /api/account/block  (block another user)
export function blockUser(blockedUserId) {
    return api.post("/api/account/block", { blockedUserId });
}