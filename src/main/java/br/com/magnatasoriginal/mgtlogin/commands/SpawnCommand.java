package br.com.magnatasoriginal.mgtlogin.commands;

import br.com.magnatasoriginal.mgtlogin.config.LoginConfig;
import br.com.magnatasoriginal.mgtlogin.data.SpawnStorage;
import br.com.magnatasoriginal.mgtlogin.data.SpawnStorage.SpawnPoint;
import br.com.magnatasoriginal.mgtlogin.util.TeleportUtil;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

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

                    TeleportUtil.teleportToSpawn(player, point);
                    player.sendSystemMessage(Component.literal("§aTeleportado para o spawn."));
                    return 1;
                }));
    }
}
