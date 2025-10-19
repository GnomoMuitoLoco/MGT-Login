package br.com.magnatasoriginal.mgtlogin.util;

import net.minecraft.server.level.ServerPlayer;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class UUIDResolver {
    public static boolean isOfflineUUID(ServerPlayer player) {
        UUID expected = UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getGameProfile().getName()).getBytes(StandardCharsets.UTF_8));
        return player.getUUID().equals(expected);
    }

    public static UUID generateOfflineUUID(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }
}
