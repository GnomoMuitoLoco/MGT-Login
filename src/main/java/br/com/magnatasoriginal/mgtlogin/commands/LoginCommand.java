package br.com.magnatasoriginal.mgtlogin.commands;

import br.com.magnatasoriginal.mgtlogin.data.AccountStorage;
import br.com.magnatasoriginal.mgtlogin.session.LoginSessionManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class LoginCommand {

    private static final SimpleCommandExceptionType NOT_REGISTERED =
            new SimpleCommandExceptionType(Component.literal("§cVocê ainda não está registrado. Use /registrar primeiro."));
    private static final SimpleCommandExceptionType WRONG_PASSWORD =
            new SimpleCommandExceptionType(Component.literal("§cSenha incorreta."));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("login")
                .then(Commands.argument("senha", StringArgumentType.word())
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            String senha = StringArgumentType.getString(ctx, "senha");

                            if (!AccountStorage.isRegistered(LoginSessionManager.getEffectiveUUID(player))) {
                                throw NOT_REGISTERED.create();
                            }

                            boolean ok = AccountStorage.verify(player, senha);
                            if (!ok) {
                                throw WRONG_PASSWORD.create();
                            }

                            LoginSessionManager.markAsAuthenticated(player);
                            player.sendSystemMessage(Component.literal("§aLogin realizado com sucesso!"));
                            return 1;
                        })
                )
        );
    }
}
