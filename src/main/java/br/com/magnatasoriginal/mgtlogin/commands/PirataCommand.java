package br.com.magnatasoriginal.mgtlogin.commands;

import br.com.magnatasoriginal.mgtlogin.util.UUIDResolver;
import br.com.magnatasoriginal.mgtlogin.session.LoginSessionManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class PirataCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pirata")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();

                    UUID fakeUUID = UUIDResolver.generateOfflineUUID(player.getGameProfile().getName());
                    LoginSessionManager.markAsPirata(player, fakeUUID);
                    player.sendSystemMessage(Component.literal("§eSua conta foi marcada como PIRATA. Agora use /registrar <senha> <repetir senha>."));
                    return 1;
                }));
    }
}
