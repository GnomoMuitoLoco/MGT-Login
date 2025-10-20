package br.com.magnatasoriginal.mgtlogin.auth;

import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import java.lang.reflect.Field;
import java.util.UUID;

public class ServerIdUtil {

    private static Field serverIdField;

    static {
        try {
            serverIdField = ServerLoginPacketListenerImpl.class.getDeclaredField("serverId");
            serverIdField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getServerId(ServerLoginPacketListenerImpl connection) {
        try {
            return (String) serverIdField.get(connection);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
