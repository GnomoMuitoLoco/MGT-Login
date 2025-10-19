package br.com.magnatasoriginal.mgtlogin.commands;

import br.com.magnatasoriginal.mgtlogin.config.LoginConfig;
import br.com.magnatasoriginal.mgtlogin.data.SpawnStorage;
import br.com.magnatasoriginal.mgtlogin.data.SpawnStorage.SpawnPoint;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class SpawnCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("spawn")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();

                    if (!LoginConfig.teleportOnCommand.get()) {
                        player.sendSystemMessage(Component.literal("§cO comando /spawn está desativado na configuração."));
                        return 0;
                    }

                    SpawnPoint point = SpawnStorage.getSpawn("default");
                    if (point == null) {
                        player.sendSystemMessage(Component.literal("§cNenhum spawn padrão foi definido."));
                        return 0;
                    }

                    teleport(player, point);
                    player.sendSystemMessage(Component.literal("§aTeleportado para o spawn."));
                    return 1;
                }));
    }

    private static void teleport(ServerPlayer player, SpawnPoint point) {
        if (player.getServer() == null) return;

        ResourceLocation dimLoc = ResourceLocation.tryParse(point.dimension());
        if (dimLoc == null) return;

        // ✅ Correção: usar Registries.DIMENSION em vez de Level.RESOURCE_KEY
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
