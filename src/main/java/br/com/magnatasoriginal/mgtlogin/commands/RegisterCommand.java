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

public class RegisterCommand {

    private static final SimpleCommandExceptionType PASSWORD_MISMATCH =
            new SimpleCommandExceptionType(Component.literal("§cAs senhas não coincidem."));
    private static final SimpleCommandExceptionType ALREADY_REGISTERED =
            new SimpleCommandExceptionType(Component.literal("§cVocê já possui uma conta registrada."));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("register")
                .then(Commands.argument("senha", StringArgumentType.word())
                        .then(Commands.argument("repetir", StringArgumentType.word())
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    String senha = StringArgumentType.getString(ctx, "senha");
                                    String repetir = StringArgumentType.getString(ctx, "repetir");

                                    // 🔹 Premium não precisa registrar
                                    if (LoginSessionManager.isMarkedPremium(player)) {
                                        player.sendSystemMessage(Component.literal("§cContas originais não precisam se registrar."));
                                        return 0;
                                    }

                                    if (!senha.equals(repetir)) {
                                        throw PASSWORD_MISMATCH.create();
                                    }

                                    // Verifica se já existe conta
                                    if (AccountStorage.isRegistered(LoginSessionManager.getEffectiveUUID(player))) {
                                        throw ALREADY_REGISTERED.create();
                                    }

                                    // Gera hash da senha
                                    String hash = PasswordUtil.hashPassword(senha);

                                    // Registra a conta como pirata
                                    boolean ok = AccountStorage.register(player, hash, false);
                                    if (!ok) {
                                        throw ALREADY_REGISTERED.create();
                                    }

                                    // Marca como autenticado e libera do limbo
                                    LoginSessionManager.markAsAuthenticated(player);
                                    player.sendSystemMessage(Component.literal("§aConta registrada com sucesso! Agora você está autenticado."));

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
