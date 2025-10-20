package br.com.magnatasoriginal.mgtlogin.session;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.*;

public class LoginSessionManager {
    private static final Set<UUID> authenticatedPlayers = new HashSet<>();
    private static final Map<UUID, Boolean> pendingPremiumFlag = new HashMap<>();
    private static final Map<UUID, UUID> overriddenUUIDs = new HashMap<>();

    // Aplica limbo total
    public static void applyLimbo(ServerPlayer player) {
        // Invulnerável e invisível
        player.setInvulnerable(true);
        player.setInvisible(true);
        player.setNoGravity(true);

        // Remove habilidades de construção
        player.getAbilities().mayBuild = false;
        player.getAbilities().instabuild = false;
        player.onUpdateAbilities();

        // Zera atributos de movimento
        var moveAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (moveAttr != null) {
            moveAttr.setBaseValue(0.0D);
        }

        var flyAttr = player.getAttribute(Attributes.FLYING_SPEED);
        if (flyAttr != null) {
            flyAttr.setBaseValue(0.0D);
        }


        // Aplica cegueira infinita
        player.addEffect(new MobEffectInstance(
                MobEffects.BLINDNESS,
                Integer.MAX_VALUE,
                1,
                false, // ambient
                false  // showParticles
        ));
    }

    // Libera do limbo após autenticação
    public static void releaseFromLimbo(ServerPlayer player) {
        player.setInvulnerable(false);
        player.setInvisible(false);
        player.setNoGravity(false);

        // Restaura habilidades padrão
        player.getAbilities().mayBuild = true;
        player.getAbilities().instabuild = false;
        player.onUpdateAbilities();

        // Restaura atributos de movimento
        var moveAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (moveAttr != null) {
            moveAttr.setBaseValue(0.1D); // valor vanilla
        }

        var flyAttr = player.getAttribute(Attributes.FLYING_SPEED);
        if (flyAttr != null) {
            flyAttr.setBaseValue(0.05D); // valor vanilla
        }

        // Remove cegueira
        player.removeEffect(MobEffects.BLINDNESS);
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

    // Já escolheu ORIGINAL ou PIRATA?
    public static boolean hasChosenAccountType(ServerPlayer player) {
        return pendingPremiumFlag.containsKey(player.getUUID());
    }

    public static UUID getEffectiveUUID(ServerPlayer player) {
        // fallback: usa UUID offline ou o próprio UUID do player
        return overriddenUUIDs.getOrDefault(player.getUUID(), player.getUUID());
    }

    public static UUID getEffectiveUUID(ServerPlayer player, String serverId) {
        if (isMarkedPremium(player)) {
            var result = br.com.magnatasoriginal.mgtlogin.auth.PremiumVerifier.verify(
                    player.getGameProfile().getName(),
                    serverId
            );

            if (result.isSuccess()) {
                // Premium confirmado → retorna UUID online
                return result.getUuid();
            } else {
                // Falhou → kicka o jogador
                player.connection.disconnect(
                        net.minecraft.network.chat.Component.literal(
                                "§cFalha na autenticação premium: " + result.getReason()
                        )
                );
                return player.getUUID(); // fallback
            }
        } else {
            // Pirata → usa UUID offline
            return overriddenUUIDs.getOrDefault(player.getUUID(), player.getUUID());
        }
    }



    public static void clearSession(ServerPlayer player) {
        UUID uuid = player.getUUID();
        authenticatedPlayers.remove(uuid);
        pendingPremiumFlag.remove(uuid);
        overriddenUUIDs.remove(uuid);

        // Garante que efeitos sejam limpos
        player.removeEffect(MobEffects.BLINDNESS);
    }
}
