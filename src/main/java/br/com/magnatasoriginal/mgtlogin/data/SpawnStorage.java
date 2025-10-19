package br.com.magnatasoriginal.mgtlogin.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

public class SpawnStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File storageFile;
    private static final Map<String, SpawnPoint> spawns = new HashMap<>();

    public static void init(File dir) {
        if (!dir.exists()) dir.mkdirs();
        storageFile = new File(dir, "spawns.json");
        load();
    }

    public static void setSpawn(String type, ServerLevel level, BlockPos pos, float yaw, float pitch) {
        spawns.put(type, new SpawnPoint(level.dimension().location().toString(), pos, yaw, pitch));
        save();
    }

    public static SpawnPoint getSpawn(String type) {
        return spawns.get(type);
    }

    private static void load() {
        if (!storageFile.exists()) return;
        try (FileReader reader = new FileReader(storageFile)) {
            Map<String, SpawnPoint> loaded = GSON.fromJson(reader, Map.class);
            if (loaded != null) spawns.putAll(loaded);
        } catch (Exception e) {
            System.err.println("[MGT-Login] Erro ao carregar spawns: " + e.getMessage());
        }
    }

    private static void save() {
        try (FileWriter writer = new FileWriter(storageFile)) {
            GSON.toJson(spawns, writer);
        } catch (Exception e) {
            System.err.println("[MGT-Login] Erro ao salvar spawns: " + e.getMessage());
        }
    }

    public record SpawnPoint(String dimension, BlockPos pos, float yaw, float pitch) {}
}
