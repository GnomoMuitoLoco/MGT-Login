package br.com.magnatasoriginal.mgtlogin.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class RegisterCommand {

    private static final SimpleCommandExceptionType PASSWORD_MISMATCH =
            new SimpleCommandExceptionType(Component.literal("§cAs senhas não coincidem."));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("register")
                .then(Commands.argument("senha", StringArgumentType.word())
                        .then(Commands.argument("repetir", StringArgumentType.word())
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    String senha = StringArgumentType.getString(ctx, "senha");
                                    String repetir = StringArgumentType.getString(ctx, "repetir");

                                    if (!senha.equals(repetir)) {
                                        throw PASSWORD_MISMATCH.create();
                                    }

                                    // Aqui você pode adicionar verificação se o jogador já está registrado
                                    // E salvar a senha com hash (ex: PasswordUtil.hash(senha))

                                    player.sendSystemMessage(Component.literal("§aConta registrada com sucesso!"));
                                    return 1;
                                })
                        )
                )
        );

        // Alias /registrar
        dispatcher.register(Commands.literal("registrar")
                .redirect(dispatcher.getRoot().getChild("register"))
        );
    }
}
