package br.com.magnatasoriginal.mgtlogin.data;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class SpawnStorage {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            // Adaptador para serializar/desserializar BlockPos
            .registerTypeAdapter(BlockPos.class, new JsonSerializer<BlockPos>() {
                @Override
                public JsonElement serialize(BlockPos src, Type typeOfSrc, JsonSerializationContext context) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("x", src.getX());
                    obj.addProperty("y", src.getY());
                    obj.addProperty("z", src.getZ());
                    return obj;
                }
            })
            .registerTypeAdapter(BlockPos.class, new JsonDeserializer<BlockPos>() {
                @Override
                public BlockPos deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    JsonObject obj = json.getAsJsonObject();
                    int x = obj.get("x").getAsInt();
                    int y = obj.get("y").getAsInt();
                    int z = obj.get("z").getAsInt();
                    return new BlockPos(x, y, z);
                }
            })
            .create();

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
            Type type = new TypeToken<Map<String, SpawnPoint>>() {}.getType();
            Map<String, SpawnPoint> loaded = GSON.fromJson(reader, type);
            if (loaded != null) {
                spawns.clear();
                spawns.putAll(loaded);
            }
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
