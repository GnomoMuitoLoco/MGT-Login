package br.com.magnatasoriginal.mgtlogin.commands;

import br.com.magnatasoriginal.mgtlogin.util.UUIDResolver;
import br.com.magnatasoriginal.mgtlogin.session.LoginSessionManager;
import br.com.magnatasoriginal.mgtlogin.data.AccountStorage;
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

                    // Verifica se já escolheu tipo de conta antes
                    boolean isFirstTime = !LoginSessionManager.hasChosenAccountType(player);

                    // Gera UUID offline baseado no nick
                    UUID fakeUUID = UUIDResolver.generateOfflineUUID(player.getGameProfile().getName());

                    // Marca como pirata
                    LoginSessionManager.markAsPirata(player, fakeUUID);

                    // Mostra mensagem APENAS na primeira vez
                    if (isFirstTime) {
                        player.sendSystemMessage(Component.literal("§cConta marcada como PIRATA."));
                    }

                    // Verifica se já tem conta registrada
                    if (AccountStorage.isRegistered(LoginSessionManager.getEffectiveUUID(player))) {
                        // Conta já existe, pedir login
                        player.sendSystemMessage(Component.literal("§eUse §f/login <senha> §epara entrar."));
                    } else {
                        // Conta nova, pedir registro
                        player.sendSystemMessage(Component.literal("§eUse §f/register <senha> <senha> §epara criar sua conta."));
                    }

                    return 1;
                }));
    }
}
