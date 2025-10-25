package br.com.magnatasoriginal.mgtlogin.commands;

import br.com.magnatasoriginal.mgtlogin.data.AccountStorage;
import br.com.magnatasoriginal.mgtlogin.data.AccountStorage.AccountData;
import br.com.magnatasoriginal.mgtlogin.session.LoginSessionManager;
import br.com.magnatasoriginal.mgtlogin.util.ModLogger;
import br.com.magnatasoriginal.mgtlogin.util.PasswordUtil;
import br.com.magnatasoriginal.mgtlogin.util.PermissionHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.UUID;

/**
 * Comandos administrativos do MGT-Login.
 * Requerem permissão magnatas.admin.mgtlogin.* ou OP nível 2+
 *
 * NOTE/TODO: Marcar pontos onde autenticação é manipulada:
 * - Chamadas para LoginSessionManager.markAsAuthenticated -> autenticação forçada
 * - Chamadas para LoginSessionManager.clearSession / applyLimbo -> manipulação de estado de limbo
 * - Uso de AccountStorage para leitura/atualização de informações de conta (autologin, last IP, etc)
 */
public class AdminCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mgtlogin")
                .requires(PermissionHelper.requires("admin"))

                // /mgtlogin forcelogin <jogador>
                .then(Commands.literal("forcelogin")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .executes(AdminCommands::forceLogin)
                        )
                )

                // /mgtlogin unlogin <jogador>
                .then(Commands.literal("unlogin")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .executes(AdminCommands::unlogin)
                        )
                )

                // /mgtlogin sessionclear <jogador>
                .then(Commands.literal("sessionclear")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .executes(AdminCommands::sessionClear)
                        )
                )

                // /mgtlogin register <jogador> <senha>
                .then(Commands.literal("register")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .then(Commands.argument("senha", StringArgumentType.word())
                                        .executes(AdminCommands::adminRegister)
                                )
                        )
                )

                // /mgtlogin changepass <jogador> <novaSenha>
                .then(Commands.literal("changepass")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .then(Commands.argument("novaSenha", StringArgumentType.word())
                                        .executes(AdminCommands::adminChangePass)
                                )
                        )
                )

                // /mgtlogin verify <jogador>
                .then(Commands.literal("verify")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .executes(AdminCommands::verify)
                        )
                )

                // /mgtlogin status
                .then(Commands.literal("status")
                        .executes(AdminCommands::status)
                )
        );
    }

    private static int forceLogin(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "jogador");

            // Autenticação forçada por admin
            LoginSessionManager.markAsAuthenticated(target); // TODO: autenticação - fonte única de verdade
            AccountStorage.updateLastLogin(target); // TODO: autologin/last IP handling resides in AccountStorage

            target.sendSystemMessage(Component.literal("§aVocê foi autenticado por um administrador."));
            ctx.getSource().sendSuccess(() -> Component.literal("§aJogador " + target.getName().getString() + " autenticado com sucesso."), true);

            ModLogger.info("Admin forçou login de: " + target.getName().getString());
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cErro: " + e.getMessage()));
            return 0;
        }
    }

    private static int unlogin(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "jogador");

            // Limpa sessão e coloca em limbo novamente
            LoginSessionManager.clearSession(target); // TODO: limbo/session handling
            LoginSessionManager.applyLimbo(target);

            target.sendSystemMessage(Component.literal("§cVocê foi deslogado por um administrador. Autentique-se novamente."));
            ctx.getSource().sendSuccess(() -> Component.literal("§aJogador " + target.getName().getString() + " foi deslogado."), true);

            ModLogger.info("Admin deslogou: " + target.getName().getString());
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cErro: " + e.getMessage()));
            return 0;
        }
    }

    private static int sessionClear(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "jogador");

            LoginSessionManager.clearSession(target); // TODO: session clear - impacts autologin behaviour

            ctx.getSource().sendSuccess(() -> Component.literal("§aSessão de " + target.getName().getString() + " foi limpa."), true);

            ModLogger.info("Admin limpou sessão de: " + target.getName().getString());
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cErro: " + e.getMessage()));
            return 0;
        }
    }

    private static int adminRegister(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "jogador");
            String senha = StringArgumentType.getString(ctx, "senha");

            if (AccountStorage.isRegistered(target.getUUID())) {
                ctx.getSource().sendFailure(Component.literal("§cJogador já possui conta registrada."));
                return 0;
            }

            String hash = PasswordUtil.hashPassword(senha);

            AccountStorage.register(target, hash); // TODO: registration persists to JSON config

            ctx.getSource().sendSuccess(() -> Component.literal("§aConta criada para " + target.getName().getString()), true);
            target.sendSystemMessage(Component.literal("§aUma conta foi criada para você por um administrador."));

            ModLogger.info("Admin registrou conta de: " + target.getName().getString());
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cErro: " + e.getMessage()));
            return 0;
        }
    }

    private static int adminChangePass(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "jogador");
            String novaSenha = StringArgumentType.getString(ctx, "novaSenha");

            if (!AccountStorage.isRegistered(target.getUUID())) {
                ctx.getSource().sendFailure(Component.literal("§cJogador não possui conta registrada."));
                return 0;
            }

            String hash = PasswordUtil.hashPassword(novaSenha);
            AccountStorage.updatePassword(target, hash);

            ctx.getSource().sendSuccess(() -> Component.literal("§aSenha alterada para " + target.getName().getString()), true);
            target.sendSystemMessage(Component.literal("§aSua senha foi alterada por um administrador."));

            ModLogger.info("Admin alterou senha de: " + target.getName().getString());
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cErro: " + e.getMessage()));
            return 0;
        }
    }

    private static int verify(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "jogador");
            UUID uuid = target.getUUID(); // use player's UUID directly
            AccountData data = AccountStorage.getAccount(uuid); // AccountStorage holds registration data persisted to disk

            ctx.getSource().sendSuccess(() -> Component.literal("§e§l═══ Informações de " + target.getName().getString() + " ═══"), false);
            ctx.getSource().sendSuccess(() -> Component.literal("§7UUID: §f" + uuid), false);

            if (data != null) {
                ctx.getSource().sendSuccess(() -> Component.literal("§7Registrado: §aSim"), false);
                ctx.getSource().sendSuccess(() -> Component.literal("§7Tipo: " + (data.premium() ? "§aORIGINAL" : "§cPIRATA")), false);
                ctx.getSource().sendSuccess(() -> Component.literal("§7Último IP: §f" + data.lastIp()), false);
                ctx.getSource().sendSuccess(() -> Component.literal("§7Data de Criação: §f" + data.creationDate()), false);
            } else {
                ctx.getSource().sendSuccess(() -> Component.literal("§7Registrado: §cNão"), false);
            }

            boolean autenticado = LoginSessionManager.isAuthenticated(target);
            ctx.getSource().sendSuccess(() -> Component.literal("§7Autenticado: " + (autenticado ? "§aSim" : "§cNão")), false);

            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cErro: " + e.getMessage()));
            return 0;
        }
    }

    private static int status(CommandContext<CommandSourceStack> ctx) {
        try {
            var server = ctx.getSource().getServer();
            Collection<ServerPlayer> players = server.getPlayerList().getPlayers();

            int online = players.size();
            int autenticados = 0;
            int original = 0;
            int pirata = 0;

            for (ServerPlayer player : players) {
                if (LoginSessionManager.isAuthenticated(player)) {
                    autenticados++;
                }
                AccountData data = AccountStorage.getAccount(player.getUUID());
                if (data != null) {
                    if (data.premium()) original++;
                    else pirata++;
                }
            }

            // ✅ FIX: Criar variáveis finais para uso em lambda
            final int finalAutenticados = autenticados;
            final int finalOriginal = original;
            final int finalPirata = pirata;

            ctx.getSource().sendSuccess(() -> Component.literal("§e§l═══ Status do Servidor ═══"), false);
            ctx.getSource().sendSuccess(() -> Component.literal("§7Jogadores Online: §f" + online), false);
            ctx.getSource().sendSuccess(() -> Component.literal("§7Autenticados: §a" + finalAutenticados), false);
            ctx.getSource().sendSuccess(() -> Component.literal("§7Contas ORIGINAL: §a" + finalOriginal), false);
            ctx.getSource().sendSuccess(() -> Component.literal("§7Contas PIRATA: §c" + finalPirata), false);

            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cErro: " + e.getMessage()));
            return 0;
        }
    }
}
