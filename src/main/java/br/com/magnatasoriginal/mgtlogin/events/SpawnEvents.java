package br.com.magnatasoriginal.mgtlogin.events;

import br.com.magnatasoriginal.mgtlogin.config.LoginConfig;
import br.com.magnatasoriginal.mgtlogin.data.SpawnStorage;
import br.com.magnatasoriginal.mgtlogin.data.SpawnStorage.SpawnPoint;
import br.com.magnatasoriginal.mgtlogin.session.LoginSessionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class SpawnEvents {

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!LoginConfig.teleportOnDeath.get()) return;

        SpawnPoint point = SpawnStorage.getSpawn("death");
        if (point != null) teleport(player, point);
    }

    @SubscribeEvent
    public void onFirstJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Detecta se é a primeira vez
        if (!player.getPersistentData().getBoolean("mgtlogin_firstJoin")) {
            player.getPersistentData().putBoolean("mgtlogin_firstJoin", true);

            // 🔹 Mostra se é Original ou Pirata
            if (LoginSessionManager.isMarkedPremium(player)) {
                player.sendSystemMessage(Component.literal("§aConta marcada como ORIGINAL (premium)."));
            } else {
                player.sendSystemMessage(Component.literal("§cConta marcada como PIRATA."));
            }

            // Teleporta se houver spawn configurado
            SpawnPoint point = SpawnStorage.getSpawn("firstjoin");
            if (point != null) teleport(player, point);
        }
    }


    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        SpawnPoint point = SpawnStorage.getSpawn("login");
        if (point != null) teleport(player, point);
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Limpa sessão antiga
        LoginSessionManager.clearSession(player);

        // Coloca no limbo
        LoginSessionManager.applyLimbo(player);

        // Mensagem inicial
        player.sendSystemMessage(Component.literal("§eSua conta é ORIGINAL ou PIRATA?"));
        player.sendSystemMessage(Component.literal("§7Responda no chat com /original ou /pirata"));
    }

    private void teleport(ServerPlayer player, SpawnPoint point) {
        if (player.getServer() == null) return;

        ResourceLocation dimLoc = ResourceLocation.tryParse(point.dimension());
        if (dimLoc == null) return;

        ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, dimLoc);
        ServerLevel level = player.getServer().getLevel(dimKey);
        if (level == null) return;

        BlockPos pos = point.pos();
        player.teleportTo(level,
                pos.getX() + 0.5,
                pos.getY(),
                pos.getZ() + 0.5,
                point.yaw(),
                point.pitch());
    }
}
