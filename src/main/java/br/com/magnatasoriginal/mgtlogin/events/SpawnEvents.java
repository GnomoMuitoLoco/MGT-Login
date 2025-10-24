package br.com.magnatasoriginal.mgtlogin.events;

import br.com.magnatasoriginal.mgtlogin.config.LoginConfig;
import br.com.magnatasoriginal.mgtlogin.data.SpawnStorage;
import br.com.magnatasoriginal.mgtlogin.data.SpawnStorage.SpawnPoint;
import br.com.magnatasoriginal.mgtlogin.session.LoginSessionManager;
import br.com.magnatasoriginal.mgtlogin.util.TeleportUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Gerencia eventos relacionados a teleportes de spawn.
 * LoginEventHandler é responsável pelo controle de sessão/autologin/limbo.
 */
public class SpawnEvents {

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!LoginConfig.teleportOnDeath.get()) return;

        SpawnPoint point = SpawnStorage.getSpawn("death");
        if (point != null) {
            TeleportUtil.teleportToSpawn(player, point);
        }
    }

    @SubscribeEvent
    public void onFirstJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Detecta se é a primeira vez
        if (!player.getPersistentData().getBoolean("mgtlogin_firstJoin")) {
            player.getPersistentData().putBoolean("mgtlogin_firstJoin", true);

            // Teleporta se houver spawn configurado
            SpawnPoint point = SpawnStorage.getSpawn("firstjoin");
            if (point != null) {
                TeleportUtil.teleportToSpawn(player, point);
            }
        }
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        SpawnPoint point = SpawnStorage.getSpawn("login");
        if (point != null) {
            TeleportUtil.teleportToSpawn(player, point);
        }
    }
}
