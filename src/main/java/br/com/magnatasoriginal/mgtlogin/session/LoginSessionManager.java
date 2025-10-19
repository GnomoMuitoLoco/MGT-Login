package br.com.magnatasoriginal.mgtlogin.session;

import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class LoginSessionManager {
    private static final Set<UUID> authenticatedPlayers = new HashSet<>();
    private static final Map<UUID, Boolean> pendingPremiumFlag = new HashMap<>();
    private static final Map<UUID, UUID> overriddenUUIDs = new HashMap<>();

    // Aplica limbo (exemplo: congelar, ocultar, etc.)
    public static void applyLimbo(ServerPlayer player) {
        player.setInvulnerable(true);
        player.setInvisible(true);
        player.setNoGravity(true);
        player.getAbilities().mayBuild = false;
        player.getAbilities().flying = true;
        player.getAbilities().mayfly = true;
        player.onUpdateAbilities();
    }

    // Libera do limbo após autenticação
    public static void releaseFromLimbo(ServerPlayer player) {
        player.setInvulnerable(false);
        player.setInvisible(false);
        player.setNoGravity(false);
        player.getAbilities().mayBuild = true;
        player.getAbilities().flying = false;
        player.getAbilities().mayfly = false;
        player.onUpdateAbilities();
    }

    // Marca como autenticado
    public static void markAsAuthenticated(ServerPlayer player) {
        authenticatedPlayers.add(player.getUUID());
        releaseFromLimbo(player);
    }

    public static boolean isAuthenticated(ServerPlayer player) {
        return authenticatedPlayers.contains(player.getUUID());
    }

    // Marca como original (premium)
    public static void markAsOriginal(ServerPlayer player) {
        pendingPremiumFlag.put(player.getUUID(), true);
    }

    // Marca como pirata e substitui UUID
    public static void markAsPirata(ServerPlayer player, UUID fakeUUID) {
        pendingPremiumFlag.put(player.getUUID(), false);
        overriddenUUIDs.put(player.getUUID(), fakeUUID);
    }

    public static boolean isMarkedPremium(ServerPlayer player) {
        return pendingPremiumFlag.getOrDefault(player.getUUID(), false);
    }

    public static UUID getEffectiveUUID(ServerPlayer player) {
        return overriddenUUIDs.getOrDefault(player.getUUID(), player.getUUID());
    }

    public static void clearSession(ServerPlayer player) {
        UUID uuid = player.getUUID();
        authenticatedPlayers.remove(uuid);
        pendingPremiumFlag.remove(uuid);
        overriddenUUIDs.remove(uuid);
    }
}
