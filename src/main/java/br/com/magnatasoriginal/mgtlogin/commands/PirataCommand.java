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

                    // Gera UUID offline baseado no nick
                    UUID fakeUUID = UUIDResolver.generateOfflineUUID(player.getGameProfile().getName());

                    // Marca como pirata
                    LoginSessionManager.markAsPirata(player, fakeUUID);

                    // Mensagem de instrução
                    player.sendSystemMessage(Component.literal(
                            "§eSua conta foi marcada como PIRATA.\n" +
                                    "§7Use §f/registrar <senha> <repetir senha> §7se for sua primeira vez.\n" +
                                    "§7Se já tiver conta, use §f/login <senha>."
                    ));

                    return 1;
                }));
    }
}
