package cc.baka9.catseedlogin.velocity;

import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;

public class Commands implements SimpleCommand {

    private final PluginMain plugin;

    public Commands(PluginMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            Config.load(plugin);
            invocation.source().sendMessage(Component.text("配置已重新加载。"));
        }
    }
}
