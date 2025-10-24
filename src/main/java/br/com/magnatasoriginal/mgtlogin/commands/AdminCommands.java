package br.com.magnatasoriginal.mgtlogin.commands;

import br.com.magnatasoriginal.mgtlogin.data.AccountStorage;
import br.com.magnatasoriginal.mgtlogin.session.LoginSessionManager;
import br.com.magnatasoriginal.mgtlogin.util.ModLogger;
import br.com.magnatasoriginal.mgtlogin.util.PasswordUtil;
import br.com.magnatasoriginal.mgtlogin.util.PermissionHelper;
import br.com.magnatasoriginal.mgtlogin.util.UUIDResolver;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

/**
 * Comandos administrativos do MGT-Login.
 * Requerem permissão magnatas.admin.mgtlogin.* ou OP nível 2+
 */
public class AdminCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mgtlogin")
                .requires(PermissionHelper.requires("admin"))

                // /mgtlogin forcelogin <jogador>
                .then(Commands.literal("forcelogin")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .executes(ctx -> forceLogin(ctx))
                        )
                )

                // /mgtlogin unlogin <jogador>
                .then(Commands.literal("unlogin")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .executes(ctx -> unlogin(ctx))
                        )
                )

                // /mgtlogin sessionclear <jogador>
                .then(Commands.literal("sessionclear")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .executes(ctx -> sessionClear(ctx))
                        )
                )

                // /mgtlogin register <jogador> <senha>
                .then(Commands.literal("register")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .then(Commands.argument("senha", StringArgumentType.word())
                                        .executes(ctx -> adminRegister(ctx))
                                )
                        )
                )

                // /mgtlogin changepass <jogador> <novaSenha>
                .then(Commands.literal("changepass")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .then(Commands.argument("novaSenha", StringArgumentType.word())
                                        .executes(ctx -> adminChangePass(ctx))
                                )
                        )
                )

                // /mgtlogin setoriginal <jogador>
                .then(Commands.literal("setoriginal")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .executes(ctx -> setOriginal(ctx))
                        )
                )

                // /mgtlogin setpirata <jogador>
                .then(Commands.literal("setpirata")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .executes(ctx -> setPirata(ctx))
                        )
                )

                // /mgtlogin verify <jogador>
                .then(Commands.literal("verify")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .executes(ctx -> verify(ctx))
                        )
                )

                // /mgtlogin status
                .then(Commands.literal("status")
                        .executes(ctx -> status(ctx))
                )
        );
    }

    private static int forceLogin(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "jogador");

            LoginSessionManager.markAsAuthenticated(target);
            AccountStorage.updateLastLogin(target);

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

            LoginSessionManager.clearSession(target);
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

            LoginSessionManager.clearSession(target);

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

            if (AccountStorage.isRegistered(LoginSessionManager.getEffectiveUUID(target))) {
                ctx.getSource().sendFailure(Component.literal("§cJogador já possui conta registrada."));
                return 0;
            }

            String hash = PasswordUtil.hashPassword(senha);
            boolean isOriginal = LoginSessionManager.isMarkedOriginal(target);

            AccountStorage.register(target, hash, isOriginal);

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

            if (!AccountStorage.isRegistered(LoginSessionManager.getEffectiveUUID(target))) {
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

    private static int setOriginal(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "jogador");

            LoginSessionManager.markAsOriginal(target);

            ctx.getSource().sendSuccess(() -> Component.literal("§aJogador " + target.getName().getString() + " marcado como ORIGINAL"), true);
            target.sendSystemMessage(Component.literal("§aSua conta foi marcada como ORIGINAL por um administrador."));

            ModLogger.info("Admin marcou como ORIGINAL: " + target.getName().getString());
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cErro: " + e.getMessage()));
            return 0;
        }
    }

    private static int setPirata(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "jogador");

            var offlineUUID = UUIDResolver.generateOfflineUUID(target.getName().getString());
            LoginSessionManager.markAsPirata(target, offlineUUID);

            ctx.getSource().sendSuccess(() -> Component.literal("§aJogador " + target.getName().getString() + " marcado como PIRATA"), true);
            target.sendSystemMessage(Component.literal("§cSua conta foi marcada como PIRATA por um administrador."));

            ModLogger.info("Admin marcou como PIRATA: " + target.getName().getString());
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cErro: " + e.getMessage()));
            return 0;
        }
    }

    private static int verify(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "jogador");
            var uuid = LoginSessionManager.getEffectiveUUID(target);
            var data = AccountStorage.getAccount(uuid);

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
                if (LoginSessionManager.isMarkedOriginal(player)) {
                    original++;
                } else if (LoginSessionManager.hasChosenAccountType(player)) {
                    pirata++;
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
