package br.com.magnatasoriginal.mgtlogin.data;

import br.com.magnatasoriginal.mgtlogin.session.LoginSessionManager;
import br.com.magnatasoriginal.mgtlogin.util.ModLogger;
import br.com.magnatasoriginal.mgtlogin.util.GsonProvider;
import br.com.magnatasoriginal.mgtlogin.util.PasswordUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.level.ServerPlayer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class AccountStorage {
    private static final Gson GSON = GsonProvider.get();
    private static final Map<UUID, AccountData> accounts = new ConcurrentHashMap<>();
    private static File storageFile;

    // Inicializa a persistência
    public static void init(Path configDir) {
        File dir = configDir.resolve("mgtlogin").toFile();
        if (!dir.exists()) dir.mkdirs();

        storageFile = new File(dir, "accounts.json");
        load();
        ModLogger.info("AccountStorage inicializado em: " + storageFile.getAbsolutePath());
    }

    // Consulta básica
    public static boolean isRegistered(UUID uuid) {
        return accounts.containsKey(uuid);
    }

    public static AccountData getAccount(UUID uuid) {
        return accounts.get(uuid);
    }

    /**
     * Registra uma nova conta.
     * - Para premium, você pode passar passwordHash como "" (não usa senha).
     * - Para pirata, use hash gerado por PasswordUtil.hashPassword.
     *
     * @param player Jogador
     * @param passwordHash Senha já com hash (ou "" para premium)
     * @param premium true se for conta original, false se for pirata
     */
    public static boolean register(ServerPlayer player, String passwordHash, boolean premium) {
        UUID uuid = LoginSessionManager.getEffectiveUUID(player);
        if (isRegistered(uuid)) return false;

        AccountData data = new AccountData(
                uuid,
                player.getGameProfile().getName(),
                passwordHash == null ? "" : passwordHash,
                player.getIpAddress(),
                Instant.now().toString(),
                premium
        );
        accounts.put(uuid, data);
        save();
        ModLogger.info("Conta registrada: " + player.getGameProfile().getName() + " (Premium: " + premium + ")");
        return true;
    }

    // Atualiza senha (apenas pirata deve ter senha)
    public static void updatePassword(ServerPlayer player, String newHash) {
        UUID uuid = LoginSessionManager.getEffectiveUUID(player);
        AccountData data = accounts.get(uuid);
        if (data != null) {
            AccountData updated = new AccountData(
                    data.uuid(),
                    data.name(),
                    newHash == null ? "" : newHash,
                    data.lastIp(),
                    data.creationDate(),
                    data.premium()
            );
            accounts.put(uuid, updated);
            save();
            ModLogger.info("Senha atualizada para: " + player.getGameProfile().getName());
        }
    }

    // Verifica senha (pirata)
    public static boolean verify(ServerPlayer player, String password) {
        UUID uuid = LoginSessionManager.getEffectiveUUID(player);
        AccountData data = accounts.get(uuid);
        if (data == null) return false;
        if (data.passwordHash() == null || data.passwordHash().isEmpty()) return false;
        return PasswordUtil.verifyPassword(password, data.passwordHash());
    }

    // Auto-login somente se nick e IP forem idênticos ao último login
    public static boolean canAutoLogin(ServerPlayer player) {
        UUID uuid = LoginSessionManager.getEffectiveUUID(player);
        AccountData data = accounts.get(uuid);
        if (data == null) return false;

        boolean sameName = data.name().equalsIgnoreCase(player.getGameProfile().getName());
        boolean sameIp = Objects.equals(data.lastIp(), player.getIpAddress());

        return sameName && sameIp;
    }

    // Atualiza IP, data e (opcional) nome ao autenticar com sucesso
    public static void updateLastLogin(ServerPlayer player) {
        UUID uuid = LoginSessionManager.getEffectiveUUID(player);
        AccountData data = accounts.get(uuid);
        if (data != null) {
            AccountData updated = new AccountData(
                    data.uuid(),
                    player.getGameProfile().getName(), // mantém sincronizado caso o nick mude
                    data.passwordHash(),
                    player.getIpAddress(),
                    Instant.now().toString(),
                    data.premium()
            );
            accounts.put(uuid, updated);
            save();
        }
    }

    // Carrega do disco
    private static void load() {
        if (!storageFile.exists()) return;
        try (FileReader reader = new FileReader(storageFile)) {
            Type type = new TypeToken<Map<UUID, AccountData>>() {}.getType();
            Map<UUID, AccountData> loaded = GSON.fromJson(reader, type);
            if (loaded != null) {
                accounts.putAll(loaded);
                ModLogger.info("Carregadas " + accounts.size() + " contas do armazenamento");
            }
        } catch (Exception e) {
            ModLogger.erro("Erro ao carregar contas", e);
        }
    }

    // Salva no disco
    private static void save() {
        try (FileWriter writer = new FileWriter(storageFile)) {
            GSON.toJson(accounts, writer);
        } catch (Exception e) {
            ModLogger.erro("Erro ao salvar contas", e);
        }
    }

    // Estrutura persistida
    public record AccountData(
            UUID uuid,
            String name,
            String passwordHash,
            String lastIp,
            String creationDate,
            boolean premium
    ) {}
}
