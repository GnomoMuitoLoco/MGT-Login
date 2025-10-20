package br.com.magnatasoriginal.mgtlogin.events;

import br.com.magnatasoriginal.mgtlogin.session.LoginSessionManager;
import br.com.magnatasoriginal.mgtlogin.data.AccountStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;

public class LoginEventHandler {

    private static final String[] ALLOWED_COMMANDS = {
            "login", "register", "original", "pirata"
    };

    // Quando o jogador entra no servidor
    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Limpa qualquer sessão antiga
        LoginSessionManager.clearSession(player);

        // 🔹 Verifica auto‑login (mesmo nick + mesmo IP)
        if (AccountStorage.canAutoLogin(player)) {
            LoginSessionManager.markAsAuthenticated(player);
            AccountStorage.updateLastLogin(player);
            player.sendSystemMessage(Component.literal("§aLogin automático realizado com sucesso!"));
            return;
        }

        // Coloca no limbo
        LoginSessionManager.applyLimbo(player);

        // Se o jogador ainda não escolheu ORIGINAL ou PIRATA
        if (!LoginSessionManager.hasChosenAccountType(player)) {
            player.sendSystemMessage(Component.literal("§eSua conta é ORIGINAL ou PIRATA?"));
            player.sendSystemMessage(Component.literal("§7Responda com /original ou /pirata"));
            return;
        }

        // Só chega aqui se já tiver escolhido ORIGINAL ou PIRATA
        var effectiveUUID = LoginSessionManager.getEffectiveUUID(player);

        if (AccountStorage.isRegistered(effectiveUUID)) {
            player.sendSystemMessage(Component.literal("§eUse /login <senha> para entrar."));
        } else {
            player.sendSystemMessage(Component.literal("§eUse /register <senha> <repetir senha> para criar sua conta."));
        }
    }

    // Bloqueia chat normal enquanto não autenticado
    @SubscribeEvent
    public void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();

        if (!LoginSessionManager.isAuthenticated(player)) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal("§cVocê precisa se autenticar para usar o chat."));
        }
    }

    // Bloqueia comandos não permitidos enquanto não autenticado
    @SubscribeEvent
    public void onCommand(CommandEvent event) {
        if (!(event.getParseResults().getContext().getSource().getEntity() instanceof ServerPlayer player)) return;

        if (!LoginSessionManager.isAuthenticated(player)) {
            String input = event.getParseResults().getReader().getString().toLowerCase();

            boolean allowed = false;
            for (String cmd : ALLOWED_COMMANDS) {
                if (input.startsWith("/" + cmd)) {
                    allowed = true;
                    break;
                }
            }

            if (!allowed) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal(
                        "§cVocê só pode usar /login, /register, /original ou /pirata até se autenticar."
                ));
            }
        }
    }

    // Bloqueia interações com botão direito em blocos
    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player && !LoginSessionManager.isAuthenticated(player)) {
            event.setCanceled(true);
        }
    }

    // Bloqueia uso de itens com botão direito
    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() instanceof ServerPlayer player && !LoginSessionManager.isAuthenticated(player)) {
            event.setCanceled(true);
        }
    }

    // Bloqueia ataques (botão esquerdo em entidades)
    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !LoginSessionManager.isAuthenticated(player)) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal("§cVocê não pode atacar antes de autenticar."));
        }
    }

    // Bloqueia cliques esquerdos em blocos (quebrar)
    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player && !LoginSessionManager.isAuthenticated(player)) {
            event.setCanceled(true);
        }
    }

    // Bloqueia quebra de blocos
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player && !LoginSessionManager.isAuthenticated(player)) {
            event.setCanceled(true);
        }
    }

    // Bloqueia colocação de blocos
    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !LoginSessionManager.isAuthenticated(player)) {
            event.setCanceled(true);
        }
    }

    // Bloqueia drop de itens
    @SubscribeEvent
    public void onItemDrop(LivingDropsEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !LoginSessionManager.isAuthenticated(player)) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal("§cVocê não pode dropar itens antes de autenticar."));
        }
    }

    // Bloqueia pickup de itens
    @SubscribeEvent
    public void onItemPickup(net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent.Pre event) {
        if (event.getPlayer() instanceof ServerPlayer player && !LoginSessionManager.isAuthenticated(player)) {
            event.setCanPickup(TriState.FALSE); // método correto no NeoForge 1.21.1
            player.sendSystemMessage(Component.literal("§cVocê não pode pegar itens antes de autenticar."));
        }
    }

    // Bloqueia abertura de inventário (inclusive mods)
    @SubscribeEvent
    public void onInventoryOpen(PlayerContainerEvent.Open event) {
        if (event.getEntity() instanceof ServerPlayer player && !LoginSessionManager.isAuthenticated(player)) {
            // Não dá para cancelar, mas dá para fechar imediatamente
            player.closeContainer();
            player.sendSystemMessage(Component.literal("§cVocê não pode abrir inventários antes de autenticar."));
        }
    }
}
