package br.com.magnatasoriginal.mgtlogin;

import br.com.magnatasoriginal.mgtlogin.commands.*;
import br.com.magnatasoriginal.mgtlogin.config.LoginConfig;
import br.com.magnatasoriginal.mgtlogin.data.AccountStorage;
import br.com.magnatasoriginal.mgtlogin.data.SpawnStorage;
import br.com.magnatasoriginal.mgtlogin.events.LoginEventHandler;
import br.com.magnatasoriginal.mgtlogin.events.SpawnEvents;
import br.com.magnatasoriginal.mgtlogin.session.LimboManager;
import br.com.magnatasoriginal.mgtlogin.util.ModLogger;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.io.File;

@Mod(MGTLogin.MODID)
public class MGTLogin {

    public static final String MODID = "mgtlogin";

    public MGTLogin(IEventBus modBus) {
        // 1) Registro da configuração (✅ correto no NeoForge)
        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.COMMON, LoginConfig.SPEC);

        // 2) Setup de inicialização comum
        modBus.addListener(this::commonSetup);

        // 3) Registro de eventos de jogo (runtime)
        // LoginEventHandler é a fonte única de verdade para controle de sessão/autologin/limbo
        NeoForge.EVENT_BUS.register(new LoginEventHandler());
        // SpawnEvents gerencia apenas teleportes
        NeoForge.EVENT_BUS.register(new SpawnEvents());
        // LimboManager gerencia tick de limbo e timeout
        NeoForge.EVENT_BUS.register(LimboManager.class);


        // 4) Registro de comandos
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        File configDir = new File("config", MODID);
        if (!configDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            configDir.mkdirs();
        }

        // Inicializa persistência de spawns
        SpawnStorage.init(configDir);

        // ✅ FIX: Passa o diretório correto, não o caminho do arquivo
        // AccountStorage.init agora resolve internamente para "mgtlogin/accounts.json"
        AccountStorage.init(configDir.toPath());

        ModLogger.info("MGT-Login inicialização concluída.");
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        LoginCommand.register(dispatcher);
        RegisterCommand.register(dispatcher);
        ChangePasswordCommand.register(dispatcher);
        SetSpawnCommand.register(dispatcher);
        SpawnCommand.register(dispatcher);
        AdminCommands.register(dispatcher); // ✅ Comandos administrativos
    }
}
