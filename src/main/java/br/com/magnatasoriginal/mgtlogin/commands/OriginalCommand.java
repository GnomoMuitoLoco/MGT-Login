package br.com.magnatasoriginal.mgtlogin.commands;

import br.com.magnatasoriginal.mgtlogin.auth.AuthResult;
import br.com.magnatasoriginal.mgtlogin.auth.PremiumVerifier;
import br.com.magnatasoriginal.mgtlogin.auth.HandshakeCache;
import br.com.magnatasoriginal.mgtlogin.data.AccountStorage;
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
                    System.out.println("[MGT-Login] 🚀 Jogador " + player.getGameProfile().getName() + " executou /original");

                    // Recupera o hash capturado no handshake usando o connectionId
                    String serverHash = HandshakeCache.get(player.getUUID());
                    if (serverHash == null) {
                        player.sendSystemMessage(Component.literal("§cNão foi possível validar sua sessão Mojang (hash ausente)."));
                        return 0;
                    }

                    // Faz a verificação premium contra a API Mojang
                    AuthResult result = PremiumVerifier.verify(player.getGameProfile().getName(), serverHash);

                    if (!result.isSuccess()) {
                        player.connection.disconnect(Component.literal("§cFalha na autenticação premium: " + result.getReason()));
                        return 0;
                    }

                    // Marca como original e autentica
                    LoginSessionManager.markAsOriginal(player);
                    LoginSessionManager.markAsAuthenticated(player);

                    // Persistência
                    if (!AccountStorage.isRegistered(result.getUuid())) {
                        AccountStorage.register(player, "", true); // premium sem senha
                    } else {
                        AccountStorage.updateLastLogin(player);
                    }

                    // Limpa o cache (usando connectionId)
                    HandshakeCache.clear(player.getUUID());

                    player.sendSystemMessage(Component.literal("§aConta validada como ORIGINAL (premium). Bem-vindo!"));
                    return 1;
                }));
    }
}
