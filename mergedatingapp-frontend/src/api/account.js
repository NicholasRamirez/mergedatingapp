import api from "./client";

// DELETE /api/account  (delete account)
export function deleteMyAccount() {
    return api.delete("/api/account");
}

// POST /api/account/block  (block another user)
export function blockUser(blockedUserId) {
    return api.post("/api/account/block", { blockedUserId });
}