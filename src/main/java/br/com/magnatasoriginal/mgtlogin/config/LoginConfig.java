package br.com.magnatasoriginal.mgtlogin.config;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ConfigValue;
import net.neoforged.fml.config.ForgeConfigSpec;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class LoginConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ConfigValue<String> uuidType;
    public static final ConfigValue<Boolean> safeLocation;
    public static final ConfigValue<Boolean> lastLocation;
    public static final ConfigValue<Boolean> teleportOnDeath;

    public static final ConfigValue<Boolean> clearChatOnJoin;
    public static final ConfigValue<Boolean> removeJoinMessage;

    public static final ConfigValue<Boolean> uiTitleBar;
    public static final ConfigValue<Boolean> uiActionBar;
    public static final ConfigValue<Boolean> uiActionBarCounter;
    public static final ConfigValue<Boolean> uiSounds;
    public static final ConfigValue<Boolean> uiChatComponent;

    public static final ConfigValue<Integer> sessionTimeout;

    static {
        BUILDER.comment("Configurações gerais do MGT-Login").push("geral");

        uuidType = BUILDER.comment("Tipo de UUID: REAL, RANDOM ou OFFLINE")
                .define("unique-id-type", "REAL");

        sessionTimeout = BUILDER.comment("Tempo de sessão em minutos (autologin por IP)")
                .define("session-timeout", 5);

        BUILDER.pop();

        BUILDER.comment("Configurações de teleport").push("teleport");

        safeLocation = BUILDER.comment("Teleportar para local seguro ao entrar")
                .define("safe-location", true);

        lastLocation = BUILDER.comment("Teleportar para última localização ao sair")
                .define("last-location", true);

        teleportOnDeath = BUILDER.comment("Teleportar para o spawn ao morrer")
                .define("teleport-on-death", true);

        BUILDER.pop();

        BUILDER.comment("Configurações de entrada").push("join");

        clearChatOnJoin = BUILDER.comment("Limpar o chat ao entrar")
                .define("clearchatonjoin", true);

        removeJoinMessage = BUILDER.comment("Remover mensagem de entrada")
                .define("removejoinmessage", true);

        BUILDER.pop();

        BUILDER.comment("Interface do usuário").push("ui");

        uiTitleBar = BUILDER.comment("Exibir título na tela")
                .define("title-bar", true);

        uiActionBar = BUILDER.comment("Exibir barra de ação")
                .define("action-bar", true);

        uiActionBarCounter = BUILDER.comment("Exibir contador na barra de ação")
                .define("actionbar-counter", true);

        uiSounds = BUILDER.comment("Ativar sons")
                .define("sounds", true);

        uiChatComponent = BUILDER.comment("Usar componentes de chat")
                .define("chat-component", true);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "mgtlogin-common.toml");
    }
}
