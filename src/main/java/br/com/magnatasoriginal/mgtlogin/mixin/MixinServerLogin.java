package br.com.magnatasoriginal.mgtlogin.mixin;

import br.com.magnatasoriginal.mgtlogin.auth.HandshakeCache;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Captura o hash Mojang (serverId hash) em handleKey e
 * migra para o HandshakeCache quando o GameProfile é definido.
 */
@Mixin(value = ServerLoginPacketListenerImpl.class, remap = false)
public class MixinServerLogin {

    // Campos da classe alvo (iguais à visibilidade que você enviou)
    @Shadow @Final
    MinecraftServer server;

    @Shadow
    @Nullable
    String requestedUsername;

    // Cache temporário por nome (antes do GameProfile existir)
    @Unique
    private static final ConcurrentHashMap<String, String> PENDING_HASHES = new ConcurrentHashMap<>();

    /**
     * Injeta no final de handleKey, recalcula o hash Mojang e guarda por nome,
     * pois o authenticatedProfile ainda não está definido nesse ponto.
     */
    @Inject(method = "handleKey", at = @At("TAIL"))
    private void mgtlogin$captureHash(ServerboundKeyPacket packet, CallbackInfo ci) {
        try {
            PrivateKey privateKey = this.server.getKeyPair().getPrivate();
            SecretKey secretKey = packet.getSecretKey(privateKey);

            String s = new BigInteger(
                    Crypt.digestData("", this.server.getKeyPair().getPublic(), secretKey)
            ).toString(16);

            if (this.requestedUsername != null) {
                PENDING_HASHES.put(this.requestedUsername, s);
                System.out.println("[MGT-Login] ✅ Hash Mojang capturado para nome " + this.requestedUsername + ": " + s);
            } else {
                System.err.println("[MGT-Login] ⚠ requestedUsername está nulo ao capturar hash");
            }
        } catch (CryptException e) {
            System.err.println("[MGT-Login] ❌ Erro ao capturar hash Mojang: " + e.getMessage());
        }
    }

    /**
     * Quando o GameProfile é definido (startClientVerification), migra o hash
     * do cache temporário (por nome) para o HandshakeCache (por UUID).
     */
    @Inject(method = "startClientVerification", at = @At("HEAD"))
    private void mgtlogin$migrateHashToUuid(GameProfile profile, CallbackInfo ci) {
        if (profile != null && profile.getId() != null && this.requestedUsername != null) {
            String pending = PENDING_HASHES.remove(this.requestedUsername);
            if (pending != null) {
                UUID uuid = profile.getId();
                HandshakeCache.store(uuid, pending);
                System.out.println("[MGT-Login] 🔄 Hash migrado para UUID " + uuid + " (" + profile.getName() + ")");
            } else {
                System.out.println("[MGT-Login] ⚠ Nenhum hash pendente para " + this.requestedUsername + " ao definir GameProfile");
            }
        }
    }
}
