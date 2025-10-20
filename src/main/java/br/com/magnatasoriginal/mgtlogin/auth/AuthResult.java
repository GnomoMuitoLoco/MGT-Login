package br.com.magnatasoriginal.mgtlogin.auth;

import java.util.UUID;

public class AuthResult {
    private final boolean success;
    private final String reason;
    private final UUID uuid;

    private AuthResult(boolean success, String reason, UUID uuid) {
        this.success = success;
        this.reason = reason;
        this.uuid = uuid;
    }

    public static AuthResult success(UUID uuid) {
        return new AuthResult(true, null, uuid);
    }

    public static AuthResult fail(String reason) {
        return new AuthResult(false, reason, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getReason() {
        return reason;
    }

    public UUID getUuid() {
        return uuid;
    }
}
