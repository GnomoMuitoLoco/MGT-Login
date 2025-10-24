package br.com.magnatasoriginal.mgtlogin.util;

import com.google.gson.*;
import net.minecraft.core.BlockPos;

import java.lang.reflect.Type;

/**
 * Provedor de instância Gson configurada com adaptadores de tipos customizados.
 * Usado por AccountStorage e SpawnStorage para serialização JSON consistente.
 */
public class GsonProvider {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(BlockPos.class, new BlockPosSerializer())
            .registerTypeAdapter(BlockPos.class, new BlockPosDeserializer())
            .create();

    public static Gson get() {
        return GSON;
    }

    /**
     * Serializa BlockPos para formato JSON: {"x": X, "y": Y, "z": Z}
     */
    private static class BlockPosSerializer implements JsonSerializer<BlockPos> {
        @Override
        public JsonElement serialize(BlockPos src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("x", src.getX());
            obj.addProperty("y", src.getY());
            obj.addProperty("z", src.getZ());
            return obj;
        }
    }

    /**
     * Desserializa JSON para BlockPos do formato: {"x": X, "y": Y, "z": Z}
     */
    private static class BlockPosDeserializer implements JsonDeserializer<BlockPos> {
        @Override
        public BlockPos deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonObject obj = json.getAsJsonObject();
            int x = obj.get("x").getAsInt();
            int y = obj.get("y").getAsInt();
            int z = obj.get("z").getAsInt();
            return new BlockPos(x, y, z);
        }
    }
}

