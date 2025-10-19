package br.com.magnatasoriginal.mgtlogin.commands;

import br.com.magnatasoriginal.mgtlogin.data.SpawnStorage;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class SetSpawnCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("setspawn")
                .executes(ctx -> setSpawn(ctx.getSource(), "default")));

        dispatcher.register(Commands.literal("setspawndeath")
                .executes(ctx -> setSpawn(ctx.getSource(), "death")));

        dispatcher.register(Commands.literal("setspawnfirstjoin")
                .executes(ctx -> setSpawn(ctx.getSource(), "firstjoin")));

        dispatcher.register(Commands.literal("setspawnlogin")
                .executes(ctx -> setSpawn(ctx.getSource(), "login")));
    }

    private static int setSpawn(CommandSourceStack source, String type) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            BlockPos pos = player.blockPosition();
            float yaw = player.getYRot();
            float pitch = player.getXRot();

            SpawnStorage.setSpawn(type, player.serverLevel(), pos, yaw, pitch);
            player.sendSystemMessage(Component.literal("§aSpawn " + type + " definido em sua posição atual."));
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cErro ao definir spawn: " + e.getMessage()));
            return 0;
        }
    }
}
