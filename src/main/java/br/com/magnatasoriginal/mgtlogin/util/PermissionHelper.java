package br.com.magnatasoriginal.mgtlogin.util;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Method;

/**
 * Gerenciamento de permissões com suporte a FTB Ranks via reflexão e fallback para OP.
 *
 * Padrão de permissão: magnatas.admin.mgtlogin.<comando>
 *
 * Exemplo: magnatas.admin.mgtlogin.setspawn
 */
public class PermissionHelper {
    private static final String PERMISSION_PREFIX = "magnatas.admin.mgtlogin.";
    private static final int DEFAULT_OP_LEVEL = 2;

    private static Boolean ftbRanksAvailable = null;
    private static Method hasPermissionMethod = null;

    /**
     * Verifica se uma fonte de comando possui a permissão necessária.
     * Primeiro tenta verificação via FTB Ranks por reflexão, depois faz fallback para nível de OP.
     *
     * @param source A fonte do comando
     * @param commandName O nome do comando (ex: "setspawn")
     * @return true se a fonte possui permissão
     */
    public static boolean hasPermission(CommandSourceStack source, String commandName) {
        return hasPermission(source, commandName, DEFAULT_OP_LEVEL);
    }

    /**
     * Verifica permissão com nível de OP customizado para fallback.
     *
     * @param source A fonte do comando
     * @param commandName O nome do comando
     * @param opLevel O nível de OP requerido se FTB Ranks não estiver disponível
     * @return true se a fonte possui permissão
     */
    public static boolean hasPermission(CommandSourceStack source, String commandName, int opLevel) {
        String permissionNode = PERMISSION_PREFIX + commandName;

        // Tenta FTB Ranks primeiro
        if (checkFTBRanks()) {
            try {
                ServerPlayer player = source.getPlayer();
                if (player != null && hasPermissionMethod != null) {
                    Object result = hasPermissionMethod.invoke(null, player.getUUID(), permissionNode);
                    if (result instanceof Boolean) {
                        ModLogger.debug("Verificação de permissão FTB Ranks para " + permissionNode + ": " + result);
                        return (Boolean) result;
                    }
                }
            } catch (Exception e) {
                ModLogger.aviso("Verificação de permissão FTB Ranks falhou, usando fallback OP: " + e.getMessage());
            }
        }

        // Fallback para nível de OP
        boolean hasOp = source.hasPermission(opLevel);
        ModLogger.debug("Verificação de permissão OP (nível " + opLevel + ") para " + commandName + ": " + hasOp);
        return hasOp;
    }

    /**
     * Verifica se FTB Ranks está disponível via reflexão.
     * Resultado é cacheado após primeira verificação.
     *
     * @return true se a API do FTB Ranks está disponível
     */
    private static boolean checkFTBRanks() {
        if (ftbRanksAvailable != null) {
            return ftbRanksAvailable;
        }

        try {
            // Tenta carregar classe da API do FTB Ranks
            Class<?> ftbRanksAPI = Class.forName("dev.ftb.mods.ftbranks.api.FTBRanksAPI");
            hasPermissionMethod = ftbRanksAPI.getMethod("hasPermission", java.util.UUID.class, String.class);
            ftbRanksAvailable = true;
            ModLogger.info("API do FTB Ranks detectada e carregada com sucesso");
        } catch (ClassNotFoundException e) {
            ftbRanksAvailable = false;
            ModLogger.info("FTB Ranks não encontrado, usando permissões baseadas em OP");
        } catch (Exception e) {
            ftbRanksAvailable = false;
            ModLogger.aviso("API do FTB Ranks encontrada mas falhou ao carregar método: " + e.getMessage());
        }

        return ftbRanksAvailable;
    }

    /**
     * Cria um predicado requires() do Brigadier para o comando dado.
     *
     * @param commandName O nome do comando
     * @return Predicado para uso em .requires()
     */
    public static java.util.function.Predicate<CommandSourceStack> requires(String commandName) {
        return source -> hasPermission(source, commandName);
    }

    /**
     * Cria um predicado requires() do Brigadier com nível de OP customizado.
     *
     * @param commandName O nome do comando
     * @param opLevel O nível de OP para fallback
     * @return Predicado para uso em .requires()
     */
    public static java.util.function.Predicate<CommandSourceStack> requires(String commandName, int opLevel) {
        return source -> hasPermission(source, commandName, opLevel);
    }
}

