package br.com.magnatasoriginal.mgtlogin.events;

import br.com.magnatasoriginal.mgtlogin.session.LoginSessionManager;
import br.com.magnatasoriginal.mgtlogin.data.AccountStorage;
import br.com.magnatasoriginal.mgtlogin.util.ModLogger;
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

/**
 * Gerencia eventos de autenticação e bloqueio de ações para jogadores não autenticados.
 * Este é a fonte única de verdade para controle de sessão/autologin/limbo.
 */
public class LoginEventHandler {

    // Comandos permitidos antes da autenticação (incluindo aliases)
    private static final String[] ALLOWED_COMMANDS = {
            "login", "logar",
            "register", "registrar",
            "original",
            "pirata",
            "authdiscord", "autenticar"
    };

    // Quando o jogador entra no servidor
    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Limpa qualquer sessão antiga
        LoginSessionManager.clearSession(player);

        // 🔹 Verifica auto-login (mesmo nick + mesmo IP)
        if (AccountStorage.canAutoLogin(player)) {
            // Marca tipo de conta baseado no registro
            var data = AccountStorage.getAccount(player.getUUID());
            if (data != null) {
                if (data.premium()) {
                    LoginSessionManager.markAsOriginal(player);
                } else {
                    LoginSessionManager.markAsPirata(player, player.getUUID());
                }
            }

            LoginSessionManager.markAsAuthenticated(player);
            AccountStorage.updateLastLogin(player);
            player.sendSystemMessage(Component.literal("§aLogin automático realizado com sucesso!"));
            ModLogger.info("Auto-login bem-sucedido para: " + player.getName().getString());
            return;
        }

        // Coloca no limbo
        LoginSessionManager.applyLimbo(player);

        // Se o jogador ainda não escolheu ORIGINAL ou PIRATA
        if (!LoginSessionManager.hasChosenAccountType(player)) {
            player.sendSystemMessage(Component.literal("§e§l═══════════════════════════════���═"));
            player.sendSystemMessage(Component.literal("§6§lBem-vindo ao servidor!"));
            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(Component.literal("§eSua conta é §aORIGINAL §eou §cPIRATA§e?"));
            player.sendSystemMessage(Component.literal("§7Use §f/original §7ou §f/pirata §7para continuar"));
            player.sendSystemMessage(Component.literal("§e§l═══════════════════════════════���"));
            return;
        }

        // Só chega aqui se já tiver escolhido ORIGINAL ou PIRATA
        var effectiveUUID = LoginSessionManager.getEffectiveUUID(player);

        if (AccountStorage.isRegistered(effectiveUUID)) {
            player.sendSystemMessage(Component.literal("§eUse §f/login <senha> §epara entrar."));
        } else {
            player.sendSystemMessage(Component.literal("§eUse §f/register <senha> <senha> §epara criar sua conta."));
        }
    }

    // Bloqueia chat normal enquanto não autenticado
    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST)
    public void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();

        if (!LoginSessionManager.isAuthenticated(player)) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal("§cVocê precisa se autenticar para usar o chat."));
            ModLogger.debug("Chat bloqueado para jogador não autenticado: " + player.getName().getString());
        }
    }

    // Bloqueia comandos não permitidos enquanto não autenticado
    @SubscribeEvent
    public void onCommand(CommandEvent event) {
        if (!(event.getParseResults().getContext().getSource().getEntity() instanceof ServerPlayer player)) return;

        if (!LoginSessionManager.isAuthenticated(player)) {
            String input = event.getParseResults().getReader().getString();

            // Debug: registra comando tentado
            ModLogger.debug("Jogador não autenticado tentou comando: " + input);

            // Remove a barra inicial para comparação
            String commandName = input.startsWith("/") ? input.substring(1).toLowerCase() : input.toLowerCase();

            boolean allowed = false;
            for (String cmd : ALLOWED_COMMANDS) {
                if (commandName.startsWith(cmd)) {
                    allowed = true;
                    ModLogger.debug("Comando permitido: " + commandName);
                    break;
                }
            }

            if (!allowed) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal(
                        "§cVocê só pode usar comandos de autenticação até se autenticar."
                ));
                player.sendSystemMessage(Component.literal(
                        "§7Comandos permitidos: §f/login§7, §f/register§7, §f/original§7, §f/pirata"
                ));
                ModLogger.debug("Comando bloqueado: " + commandName);
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
        }
    }

    // Bloqueia pickup de itens
    @SubscribeEvent
    public void onItemPickup(net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent.Pre event) {
        if (event.getPlayer() instanceof ServerPlayer player && !LoginSessionManager.isAuthenticated(player)) {
            event.setCanPickup(TriState.FALSE);
        }
    }

    // Bloqueia abertura de inventário (inclusive mods)
    @SubscribeEvent
    public void onInventoryOpen(PlayerContainerEvent.Open event) {
        if (event.getEntity() instanceof ServerPlayer player && !LoginSessionManager.isAuthenticated(player)) {
            player.closeContainer();
        }
    }

    // ========== PROTEÇÃO ESPECÍFICA PARA FTB QUESTS ==========

    /**
     * Verifica se um item pertence ao FTB Quests (como o quest book)
     */
    private boolean isFTBQuestsItem(net.minecraft.world.item.ItemStack itemStack) {
        if (itemStack.isEmpty()) return false;

        try {
            var itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(itemStack.getItem());
            return itemId.getNamespace().equals("ftbquests");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Proteção adicional: bloqueia especificamente itens do FTB Quests
     * (O FTB Quests não usa containers normais, usa Screen customizada)
     */
    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST)
    public void onFTBQuestsItemUse(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (!LoginSessionManager.isAuthenticated(player) && isFTBQuestsItem(event.getItemStack())) {
            event.setCanceled(true);
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("§c§l[SEGURANÇA] §cFaça login para usar o FTB Quests!"),
                true
            );
            ModLogger.aviso("Bloqueado uso do FTB Quests por " + player.getName().getString() + " (não autenticado)");
        }
    }
}
