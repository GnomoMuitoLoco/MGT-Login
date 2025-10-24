package br.com.magnatasoriginal.mgtlogin.util;

import br.com.magnatasoriginal.mgtlogin.data.SpawnStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * Utilitário unificado de teleporte para pontos de spawn.
 * Consolida lógica de teleporte anteriormente duplicada em SpawnCommand e SpawnEvents.
 */
public class TeleportUtil {

    /**
     * Teleporta um jogador para o ponto de spawn especificado.
     *
     * @param player O jogador a ser teleportado
     * @param point O ponto de spawn de destino
     * @return true se o teleporte foi bem-sucedido, false caso contrário
     */
    public static boolean teleportToSpawn(ServerPlayer player, SpawnStorage.SpawnPoint point) {
        if (player.getServer() == null) {
            ModLogger.aviso("Não é possível teleportar o jogador " + player.getName().getString() + ": servidor é nulo");
            return false;
        }

        ResourceLocation dimLoc = ResourceLocation.tryParse(point.dimension());
        if (dimLoc == null) {
            ModLogger.aviso("Localização de dimensão inválida: " + point.dimension());
            return false;
        }

        ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, dimLoc);
        ServerLevel level = player.getServer().getLevel(dimKey);

        if (level == null) {
            ModLogger.aviso("Dimensão não encontrada: " + dimLoc);
            return false;
        }

        BlockPos pos = point.pos();
        player.teleportTo(
            level,
            pos.getX() + 0.5,
            pos.getY(),
            pos.getZ() + 0.5,
            point.yaw(),
            point.pitch()
        );

        ModLogger.debug("Teleportou " + player.getName().getString() + " para " + point.dimension() +
                       " em " + pos.getX() + "," + pos.getY() + "," + pos.getZ());
        return true;
    }
}
