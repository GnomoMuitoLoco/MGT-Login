package br.com.magnatasoriginal.mgtlogin;

import br.com.magnatasoriginal.mgtlogin.config.LoginConfig;
import br.com.magnatasoriginal.mgtlogin.commands.RegisterCommand;
import br.com.magnatasoriginal.mgtlogin.commands.LoginCommand;
import br.com.magnatasoriginal.mgtlogin.commands.ChangePasswordCommand;
import br.com.magnatasoriginal.mgtlogin.commands.SetSpawnCommand;
import br.com.magnatasoriginal.mgtlogin.events.LoginEventHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.fml.common.Mod;

@Mod(MGTLogin.MODID)
public class MGTLogin {
    public static final String MODID = "mgtlogin";

    public MGTLogin() {
        IEventBus modBus = NeoForge.MOD_BUS;

        // Registra configuração
        LoginConfig.register();

        // Registra eventos do lado do servidor
        NeoForge.EVENT_BUS.register(new LoginEventHandler());
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        RegisterCommand.register(event.getDispatcher());
        LoginCommand.register(event.getDispatcher());
        ChangePasswordCommand.register(event.getDispatcher());
        SetSpawnCommand.register(event.getDispatcher());
    }
}
