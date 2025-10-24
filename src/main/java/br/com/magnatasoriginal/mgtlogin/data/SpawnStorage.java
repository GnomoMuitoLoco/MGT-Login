package br.com.magnatasoriginal.mgtlogin.data;

import br.com.magnatasoriginal.mgtlogin.util.GsonProvider;
import br.com.magnatasoriginal.mgtlogin.util.ModLogger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SpawnStorage {
    private static final Gson GSON = GsonProvider.get();
    private static File storageFile;
    private static final Map<String, SpawnPoint> spawns = new ConcurrentHashMap<>();

    public static void init(File dir) {
        if (!dir.exists()) dir.mkdirs();
        storageFile = new File(dir, "spawns.json");
        load();
        ModLogger.info("SpawnStorage inicializado em: " + storageFile.getAbsolutePath());
    }

    public static void setSpawn(String type, ServerLevel level, BlockPos pos, float yaw, float pitch) {
        spawns.put(type, new SpawnPoint(level.dimension().location().toString(), pos, yaw, pitch));
        save();
        ModLogger.info("Spawn '" + type + "' definido em " + level.dimension().location() + " " + pos);
    }

    public static SpawnPoint getSpawn(String type) {
        return spawns.get(type);
    }

    private static void load() {
        if (!storageFile.exists()) return;
        try (FileReader reader = new FileReader(storageFile)) {
            Type type = new TypeToken<Map<String, SpawnPoint>>() {}.getType();
            Map<String, SpawnPoint> loaded = GSON.fromJson(reader, type);
            if (loaded != null) {
                spawns.clear();
                spawns.putAll(loaded);
                ModLogger.info("Carregados " + spawns.size() + " pontos de spawn");
            }
        } catch (Exception e) {
            ModLogger.erro("Erro ao carregar spawns", e);
        }
    }

    private static void save() {
        try (FileWriter writer = new FileWriter(storageFile)) {
            GSON.toJson(spawns, writer);
        } catch (Exception e) {
            ModLogger.erro("Erro ao salvar spawns", e);
        }
    }

    public record SpawnPoint(String dimension, BlockPos pos, float yaw, float pitch) {}
}
