package br.com.magnatasoriginal.mgtlogin.auth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class PremiumVerifier {

    public static AuthResult verify(String username, String serverHash) {
        try {
            String urlStr = "https://sessionserver.mojang.com/session/minecraft/hasJoined?username="
                    + username + "&serverId=" + serverHash;

            System.out.println("[MGT-Login] 🔎 Consultando Mojang API: " + urlStr);

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int code = conn.getResponseCode();
            System.out.println("[MGT-Login] 🌐 Resposta HTTP: " + code);

            if (code == 200) {
                JsonObject json = JsonParser.parseReader(new InputStreamReader(conn.getInputStream())).getAsJsonObject();
                String id = json.get("id").getAsString();
                UUID uuid = parseUUID(id);

                System.out.println("[MGT-Login] ✅ Sessão validada para " + username + " (UUID: " + uuid + ")");
                return AuthResult.success(uuid);
            } else {
                return AuthResult.fail("Mojang rejeitou a sessão (código " + code + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return AuthResult.fail("Erro ao contatar API Mojang: " + e.getMessage());
        }
    }


    private static UUID parseUUID(String id) {
        // Converte string sem hífen em UUID
        return UUID.fromString(id.replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                "$1-$2-$3-$4-$5"
        ));
    }
}
