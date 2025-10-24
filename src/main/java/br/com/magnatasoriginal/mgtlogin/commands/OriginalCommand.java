package br.com.magnatasoriginal.mgtlogin.commands;

import br.com.magnatasoriginal.mgtlogin.data.AccountStorage;
import br.com.magnatasoriginal.mgtlogin.session.LoginSessionManager;
import br.com.magnatasoriginal.mgtlogin.util.ModLogger;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Comando /original
 * Marca a conta do jogador como ORIGINAL (premium).
 * Permitido ANTES da autenticação.
 */
public class OriginalCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("original")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    ModLogger.info("Jogador " + player.getGameProfile().getName() + " executou /original");

                    // Verifica se já escolheu tipo de conta antes
                    boolean isFirstTime = !LoginSessionManager.hasChosenAccountType(player);

                    // Marca como original (sem verificação Mojang)
                    LoginSessionManager.markAsOriginal(player);

                    // Mostra mensagem APENAS na primeira vez
                    if (isFirstTime) {
                        player.sendSystemMessage(Component.literal("§aConta marcada como ORIGINAL."));
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
