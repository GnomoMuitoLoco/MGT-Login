package br.com.magnatasoriginal.mgtlogin.commands;

import br.com.magnatasoriginal.mgtlogin.data.AccountStorage;
import br.com.magnatasoriginal.mgtlogin.session.LoginSessionManager;
import br.com.magnatasoriginal.mgtlogin.util.PasswordUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class LoginCommand {

    private static final SimpleCommandExceptionType NOT_REGISTERED =
            new SimpleCommandExceptionType(Component.literal("§cVocê não possui uma conta registrada."));
    private static final SimpleCommandExceptionType WRONG_PASSWORD =
            new SimpleCommandExceptionType(Component.literal("§cSenha incorreta."));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("login")
                .then(Commands.argument("senha", StringArgumentType.word())
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            String senha = StringArgumentType.getString(ctx, "senha");

                            // Recupera a conta pelo UUID do jogador
                            AccountStorage.AccountData data =
                                    AccountStorage.getAccount(player.getUUID());

                            if (data == null) {
                                throw NOT_REGISTERED.create();
                            }

                            // Verifica senha
                            if (!PasswordUtil.verifyPassword(senha, data.passwordHash())) {
                                throw WRONG_PASSWORD.create();
                            }

                            // Marca como autenticado e libera do limbo
                            LoginSessionManager.markAsAuthenticated(player);

                            // Atualiza IP e data de login
                            AccountStorage.updateLastLogin(player);

                            player.sendSystemMessage(Component.literal("§aLogin realizado com sucesso!"));

                            return 1;
                        })
                )
        );

        // Alias /logar
        dispatcher.register(Commands.literal("logar")
                .redirect(dispatcher.getRoot().getChild("login"))
        );
    }
}
