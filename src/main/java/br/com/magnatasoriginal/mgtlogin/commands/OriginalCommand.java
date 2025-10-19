package br.com.magnatasoriginal.mgtlogin.commands;

import br.com.magnatasoriginal.mgtlogin.util.UUIDResolver;
import br.com.magnatasoriginal.mgtlogin.session.LoginSessionManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class OriginalCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("original")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();

                    if (UUIDResolver.isOfflineUUID(player)) {
                        player.sendSystemMessage(Component.literal("§cSua conta parece ser pirata. Use /pirata."));
                        return 0;
                    }

                    LoginSessionManager.markAsOriginal(player);
                    player.sendSystemMessage(Component.literal("§aSua conta foi marcada como ORIGINAL. Agora use /registrar <senha> <repetir senha>."));

                    return 1;
                }));
    }
}
