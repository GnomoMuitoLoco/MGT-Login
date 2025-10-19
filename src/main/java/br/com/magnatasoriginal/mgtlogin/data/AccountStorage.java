package br.com.magnatasoriginal.mgtlogin.data;

import br.com.magnatasoriginal.mgtlogin.session.LoginSessionManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.level.ServerPlayer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

import br.com.magnatasoriginal.mgtlogin.util.PasswordUtil;

public class AccountStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<UUID, AccountData> accounts = new HashMap<>();
    private static File storageFile;

    public static void init(Path configDir) {
        File dir = configDir.resolve("mgtlogin").toFile();
        if (!dir.exists()) dir.mkdirs();

        storageFile = new File(dir, "accounts.json");
        load();
    }

    public static boolean isRegistered(UUID uuid) {
        return accounts.containsKey(uuid);
    }

    public static AccountData getAccount(UUID uuid) {
        return accounts.get(uuid);
    }

    /**
     * Registra uma nova conta.
     * @param player Jogador
     * @param passwordHash Senha já com hash
     * @param premium true se for conta original, false se for pirata
     */
    public static boolean register(ServerPlayer player, String passwordHash, boolean premium) {
        UUID uuid = LoginSessionManager.getEffectiveUUID(player);
        if (isRegistered(uuid)) return false;

        AccountData data = new AccountData(
                uuid,
                player.getGameProfile().getName(),
                passwordHash,
                player.getIpAddress(),
                Instant.now().toString(),
                premium
        );
        accounts.put(uuid, data);
        save();
        return true;
    }

    public static void updatePassword(ServerPlayer player, String newHash) {
        UUID uuid = LoginSessionManager.getEffectiveUUID(player);
        AccountData data = accounts.get(uuid);
        if (data != null) {
            AccountData updated = new AccountData(
                    data.uuid(),
                    data.name(),
                    newHash,
                    data.lastIp(),
                    data.creationDate(),
                    data.premium()
            );
            accounts.put(uuid, updated);
            save();
        }
    }

    public static boolean verify(ServerPlayer player, String password) {
        UUID uuid = LoginSessionManager.getEffectiveUUID(player);
        AccountData data = accounts.get(uuid);
        if (data == null) return false;
        return PasswordUtil.verifyPassword(password, data.passwordHash());
    }


    private static void load() {
        if (!storageFile.exists()) return;
        try (FileReader reader = new FileReader(storageFile)) {
            Type type = new TypeToken<Map<UUID, AccountData>>() {}.getType();
            Map<UUID, AccountData> loaded = GSON.fromJson(reader, type);
            if (loaded != null) accounts.putAll(loaded);
        } catch (Exception e) {
            System.err.println("[MGT-Login] Erro ao carregar contas: " + e.getMessage());
        }
    }

    private static void save() {
        try (FileWriter writer = new FileWriter(storageFile)) {
            GSON.toJson(accounts, writer);
        } catch (Exception e) {
            System.err.println("[MGT-Login] Erro ao salvar contas: " + e.getMessage());
        }
    }

    // Agora com o campo premium
    public record AccountData(
            UUID uuid,
            String name,
            String passwordHash,
            String lastIp,
            String creationDate,
            boolean premium
    ) {}
}
