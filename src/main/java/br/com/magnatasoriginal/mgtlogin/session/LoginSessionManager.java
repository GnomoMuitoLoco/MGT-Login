package br.com.magnatasoriginal.mgtlogin.session;

import br.com.magnatasoriginal.mgtlogin.util.ModLogger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerenciador de sessões de login.
 * Controla estado de autenticação, limbo e tipo de conta (Original/Pirata).
 *
 * Autenticação é puramente local (sem verificação Mojang).
 */
public class LoginSessionManager {
    private static final Set<UUID> authenticatedPlayers = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Boolean> accountTypeOriginal = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> overriddenUUIDs = new ConcurrentHashMap<>();

    /**
     * Aplica limbo total usando o LimboManager.
     */
    public static void applyLimbo(ServerPlayer player) {
        // Usa o novo LimboManager que gerencia tudo
        LimboManager.enterLimbo(player);

        // Aplica imobilização adicional via atributos
        var moveAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (moveAttr != null) {
            moveAttr.setBaseValue(0.0D);
        }

        var flyAttr = player.getAttribute(Attributes.FLYING_SPEED);
        if (flyAttr != null) {
            flyAttr.setBaseValue(0.0D);
        }

        // Remove habilidades
        player.getAbilities().mayBuild = false;
        player.getAbilities().instabuild = false;
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.onUpdateAbilities();

        ModLogger.debug("Limbo aplicado para: " + player.getName().getString());
    }

    /**
     * Libera do limbo usando o LimboManager.
     */
    public static void releaseFromLimbo(ServerPlayer player) {
        // Usa o LimboManager para restaurar inventário
        LimboManager.exitLimbo(player);

        // Restaura atributos de movimento
        var moveAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (moveAttr != null) {
            moveAttr.setBaseValue(0.1D); // valor vanilla
        }

        var flyAttr = player.getAttribute(Attributes.FLYING_SPEED);
        if (flyAttr != null) {
            flyAttr.setBaseValue(0.05D); // valor vanilla
        }

        // Restaura habilidades padrão
        player.getAbilities().mayBuild = true;
        player.getAbilities().instabuild = false;
        player.onUpdateAbilities();

        ModLogger.debug("Limbo removido para: " + player.getName().getString());
    }

    // Marca como autenticado
    public static void markAsAuthenticated(ServerPlayer player) {
        authenticatedPlayers.add(player.getUUID());
        releaseFromLimbo(player);
        ModLogger.info("Jogador autenticado: " + player.getName().getString());
    }

    public static boolean isAuthenticated(ServerPlayer player) {
        return authenticatedPlayers.contains(player.getUUID());
    }

    // Marca como original (conta premium/original)
    public static void markAsOriginal(ServerPlayer player) {
        accountTypeOriginal.put(player.getUUID(), true);
        ModLogger.debug("Jogador marcado como ORIGINAL: " + player.getName().getString());
    }

    // Marca como pirata e substitui UUID
    public static void markAsPirata(ServerPlayer player, UUID offlineUUID) {
        accountTypeOriginal.put(player.getUUID(), false);
        overriddenUUIDs.put(player.getUUID(), offlineUUID);
        ModLogger.debug("Jogador marcado como PIRATA: " + player.getName().getString());
    }

    public static boolean isMarkedOriginal(ServerPlayer player) {
        return accountTypeOriginal.getOrDefault(player.getUUID(), false);
    }

    // Já escolheu ORIGINAL ou PIRATA?
    public static boolean hasChosenAccountType(ServerPlayer player) {
        return accountTypeOriginal.containsKey(player.getUUID());
    }

    /**
     * Retorna o UUID efetivo do jogador.
     * - Para contas ORIGINAL: usa o UUID da conexão atual
     * - Para contas PIRATA: usa UUID offline gerado
     */
    public static UUID getEffectiveUUID(ServerPlayer player) {
        return overriddenUUIDs.getOrDefault(player.getUUID(), player.getUUID());
    }

    public static void clearSession(ServerPlayer player) {
        UUID uuid = player.getUUID();
        authenticatedPlayers.remove(uuid);
        accountTypeOriginal.remove(uuid);
        overriddenUUIDs.remove(uuid);

        // Limpa dados do LimboManager
        LimboManager.clearLimboData(uuid);

        ModLogger.debug("Sessão limpa para: " + player.getName().getString());
    }
}
