package br.com.magnatasoriginal.mgtlogin.auth;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Cache temporário para armazenar o hash Mojang (serverId hash)
 * capturado no handshake de login.
 *
 * A chave usada é o connectionId (UUID único da conexão).
 */
public class HandshakeCache {

    // Mapa thread-safe para múltiplos jogadores simultâneos
    private static final ConcurrentMap<UUID, String> CACHE = new ConcurrentHashMap<>();

    /**
     * Armazena o hash associado a uma conexão.
     *
     * @param connectionId UUID único da conexão (player.connection.connectionId)
     * @param hash         Hash calculado no handshake
     */
    public static void store(UUID connectionId, String hash) {
        if (connectionId != null && hash != null) {
            CACHE.put(connectionId, hash);
        }
    }

    /**
     * Recupera o hash associado a uma conexão.
     *
     * @param connectionId UUID único da conexão
     * @return Hash ou null se não existir
     */
    public static String get(UUID connectionId) {
        if (connectionId == null) return null;
        return CACHE.get(connectionId);
    }

    /**
     * Remove o hash associado a uma conexão (limpeza após uso).
     *
     * @param connectionId UUID único da conexão
     */
    public static void clear(UUID connectionId) {
        if (connectionId != null) {
            CACHE.remove(connectionId);
        }
    }

    /**
     * Limpa todo o cache (caso precise resetar).
     */
    public static void clearAll() {
        CACHE.clear();
    }
}
