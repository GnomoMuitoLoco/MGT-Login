package br.com.magnatasoriginal.mgtlogin.auth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

public class MojangApiClient {

    private static final String PROFILE_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String SESSION_URL = "https://sessionserver.mojang.com/session/minecraft/hasJoined";

    /**
     * Consulta a Mojang para verificar se o nick existe como premium.
     * @param nickname Nome do jogador
     * @return UUID online se existir, Optional.empty() se não existir
     */
    public static Optional<UUID> getPremiumUUID(String nickname) {
        try {
            URL url = new URL(PROFILE_URL + nickname);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() == 200) {
                JsonObject json = JsonParser.parseReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
                ).getAsJsonObject();

                String id = json.get("id").getAsString();
                // Converte string sem hífen para UUID
                UUID uuid = UUID.fromString(
                        id.replaceFirst(
                                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                                "$1-$2-$3-$4-$5"
                        )
                );
                return Optional.of(uuid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Verifica no sessionserver se o jogador realmente é dono da conta.
     * IMPORTANTE: só funciona durante handshake real (serverId válido).
     * @param nickname Nome do jogador
     * @param serverId Hash gerado no handshake
     * @return true se a Mojang confirmar, false caso contrário
     */
    public static boolean validateSession(String nickname, String serverId) {
        try {
            String urlStr = SESSION_URL + "?username=" + nickname + "&serverId=" + serverId;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            return conn.getResponseCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
