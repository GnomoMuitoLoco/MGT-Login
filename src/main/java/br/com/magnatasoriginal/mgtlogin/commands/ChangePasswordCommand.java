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

public class ChangePasswordCommand {

    private static final SimpleCommandExceptionType NOT_AUTHENTICATED =
            new SimpleCommandExceptionType(Component.literal("§cVocê precisa estar logado para mudar a senha."));
    private static final SimpleCommandExceptionType WRONG_PASSWORD =
            new SimpleCommandExceptionType(Component.literal("§cSenha atual incorreta."));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Comando principal
        dispatcher.register(Commands.literal("changepassword")
                .then(Commands.argument("senha_atual", StringArgumentType.word())
                        .then(Commands.argument("senha_nova", StringArgumentType.word())
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    String senhaAtual = StringArgumentType.getString(ctx, "senha_atual");
                                    String senhaNova = StringArgumentType.getString(ctx, "senha_nova");

                                    if (!LoginSessionManager.isAuthenticated(player)) {
                                        throw NOT_AUTHENTICATED.create();
                                    }

                                    if (!AccountStorage.verify(player, senhaAtual)) {
                                        throw WRONG_PASSWORD.create();
                                    }

                                    String hash = PasswordUtil.hashPassword(senhaNova);
                                    AccountStorage.updatePassword(player, hash);

                                    player.sendSystemMessage(Component.literal("§aSenha alterada com sucesso!"));
                                    return 1;
                                })
                        )
                )
        );

        // Aliases
        dispatcher.register(Commands.literal("mudarsenha")
                .redirect(dispatcher.getRoot().getChild("changepassword")));
        dispatcher.register(Commands.literal("trocarsenha")
                .redirect(dispatcher.getRoot().getChild("changepassword")));
    }
}
