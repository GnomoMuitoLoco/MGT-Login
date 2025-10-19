package br.com.magnatasoriginal.mgtlogin.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class LoginConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    // Geral
    public static final ModConfigSpec.ConfigValue<String> uuidType;
    public static final ModConfigSpec.IntValue sessionTimeout;

    // Teleport
    public static final ModConfigSpec.BooleanValue safeLocation;
    public static final ModConfigSpec.BooleanValue lastLocation;
    public static final ModConfigSpec.BooleanValue teleportOnDeath;
    public static final ModConfigSpec.BooleanValue teleportOnFirstJoin;
    public static final ModConfigSpec.BooleanValue teleportOnLogin;
    public static final ModConfigSpec.BooleanValue teleportOnCommand;

    // Entrada
    public static final ModConfigSpec.BooleanValue clearChatOnJoin;
    public static final ModConfigSpec.BooleanValue removeJoinMessage;

    // UI
    public static final ModConfigSpec.BooleanValue uiTitleBar;
    public static final ModConfigSpec.BooleanValue uiActionBar;
    public static final ModConfigSpec.BooleanValue uiActionBarCounter;
    public static final ModConfigSpec.BooleanValue uiSounds;
    public static final ModConfigSpec.BooleanValue uiChatComponent;

    static {
        BUILDER.comment("Configurações gerais do MGT-Login").push("geral");

        uuidType = BUILDER.comment("Tipo de UUID: REAL, RANDOM ou OFFLINE")
                .define("unique-id-type", "REAL");

        sessionTimeout = BUILDER.comment("Tempo de sessão em minutos (autologin por IP)")
                .defineInRange("session-timeout", 5, 1, 1440);

        BUILDER.pop();

        BUILDER.comment("Configurações de teleport").push("teleport");

        safeLocation = BUILDER.comment("Teleportar para local seguro ao entrar")
                .define("safe-location", true);

        lastLocation = BUILDER.comment("Teleportar para última localização ao sair")
                .define("last-location", true);

        teleportOnDeath = BUILDER.comment("Teleportar para o spawn ao morrer")
                .define("teleport-on-death", true);

        teleportOnFirstJoin = BUILDER.comment("Teleportar para o spawn da primeira vez que o jogador entra")
                .define("teleport-on-first-join", true);

        teleportOnLogin = BUILDER.comment("Teleportar para o spawn ao logar")
                .define("teleport-on-login", true);

        teleportOnCommand = BUILDER.comment("Permitir /spawn para teleportar ao spawn padrão")
                .define("teleport-on-command", true);

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
}
