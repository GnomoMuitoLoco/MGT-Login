package br.com.magnatasoriginal.mgtlogin.session;

import br.com.magnatasoriginal.mgtlogin.config.LoginConfig;
import br.com.magnatasoriginal.mgtlogin.util.ModLogger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerenciador de limbo avançado.
 * Trava jogador no lugar, esconde inventário e aplica timeout de sessão.
 */
public class LimboManager {

    // Posições de spawn do limbo para cada jogador
    private static final Map<UUID, LimboData> limboPlayers = new ConcurrentHashMap<>();

    /**
     * Coloca um jogador em limbo.
     * Guarda inventário e posição inicial.
     */
    public static void enterLimbo(ServerPlayer player) {
        BlockPos spawnPos = player.blockPosition();
        float spawnYaw = player.getYRot();
        float spawnPitch = player.getXRot();

        // Guarda inventário
        ItemStack[] savedInventory = new ItemStack[player.getInventory().getContainerSize()];
        for (int i = 0; i < savedInventory.length; i++) {
            savedInventory[i] = player.getInventory().getItem(i).copy();
        }

        // Limpa inventário atual
        player.getInventory().clearContent();

        // Registra no limbo
        limboPlayers.put(player.getUUID(), new LimboData(
            spawnPos, spawnYaw, spawnPitch, savedInventory, 0
        ));

        // Aplica efeitos visuais
        applyLimboEffects(player);

        ModLogger.info("Jogador entrou em limbo: " + player.getName().getString());
    }

    /**
     * Libera um jogador do limbo.
     * Restaura inventário e remove efeitos.
     */
    public static void exitLimbo(ServerPlayer player) {
        LimboData data = limboPlayers.remove(player.getUUID());
        if (data == null) return;

        // Restaura inventário
        Inventory inv = player.getInventory();
        inv.clearContent();
        for (int i = 0; i < data.savedInventory.length; i++) {
            inv.setItem(i, data.savedInventory[i]);
        }

        // Remove efeitos
        removeLimboEffects(player);

        ModLogger.info("Jogador saiu do limbo: " + player.getName().getString());
    }

    /**
     * Verifica se um jogador está em limbo.
     */
    public static boolean isInLimbo(ServerPlayer player) {
        return limboPlayers.containsKey(player.getUUID());
    }

    /**
     * Tick handler - atualiza jogadores em limbo a cada tick.
     * Reseta posição, rotação e verifica timeout.
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        LimboData data = limboPlayers.get(player.getUUID());
        if (data == null) return;

        // Reseta posição
        player.teleportTo(
            data.position.getX() + 0.5,
            data.position.getY(),
            data.position.getZ() + 0.5
        );

        // ✅ CORREÇÃO: Reseta rotação da câmera (yaw e pitch)
        player.setYRot(data.yaw);
        player.setXRot(data.pitch);
        player.setYHeadRot(data.yaw); // Também trava a rotação da cabeça

        // Zera velocidade
        player.setDeltaMovement(0, 0, 0);

        // Incrementa contador de timeout
        data.ticksInLimbo++;

        // Pega timeout configurável (em minutos)
        int timeoutMinutes = LoginConfig.limboTimeoutMinutes.get();
        int timeoutTicks = timeoutMinutes * 1200; // 1 minuto = 1200 ticks

        // Verifica timeout
        if (data.ticksInLimbo >= timeoutTicks) {
            player.connection.disconnect(
                net.minecraft.network.chat.Component.literal(
                    "§cTempo de autenticação esgotado (" + timeoutMinutes + " minutos). Reconecte e autentique-se."
                )
            );
            limboPlayers.remove(player.getUUID());
            ModLogger.info("Jogador kickado por timeout de limbo: " + player.getName().getString());
        }

        // Aviso a cada minuto (1200 ticks)
        if (data.ticksInLimbo % 1200 == 0 && data.ticksInLimbo > 0) {
            int minutesLeft = (timeoutTicks - data.ticksInLimbo) / 1200;
            if (minutesLeft > 0) {
                player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal(
                        "§eAviso: Você será desconectado em " + minutesLeft + " minuto(s) se não autenticar."
                    )
                );
            }
        }
    }

    /**
     * Aplica efeitos visuais do limbo.
     */
    private static void applyLimboEffects(ServerPlayer player) {
        // Cegueira infinita (usando amplificador 255 e duração máxima)
        player.addEffect(new MobEffectInstance(
            MobEffects.BLINDNESS,
            Integer.MAX_VALUE,
            255,
            false,
            false
        ));

        // Slow falling infinito para evitar queda
        player.addEffect(new MobEffectInstance(
            MobEffects.SLOW_FALLING,
            Integer.MAX_VALUE,
            0,
            false,
            false
        ));
    }

    /**
     * Remove efeitos do limbo.
     */
    private static void removeLimboEffects(ServerPlayer player) {
        player.removeEffect(MobEffects.BLINDNESS);
        player.removeEffect(MobEffects.SLOW_FALLING);
    }

    /**
     * Limpa dados de limbo (usado ao desconectar).
     */
    public static void clearLimboData(UUID playerUUID) {
        limboPlayers.remove(playerUUID);
    }

    /**
     * Dados de limbo de um jogador.
     */
    private static class LimboData {
        final BlockPos position;
        final float yaw;
        final float pitch;
        final ItemStack[] savedInventory;
        int ticksInLimbo;

        LimboData(BlockPos position, float yaw, float pitch, ItemStack[] savedInventory, int ticksInLimbo) {
            this.position = position;
            this.yaw = yaw;
            this.pitch = pitch;
            this.savedInventory = savedInventory;
            this.ticksInLimbo = ticksInLimbo;
        }
    }
}

