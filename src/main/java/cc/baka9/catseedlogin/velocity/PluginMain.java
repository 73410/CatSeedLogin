package cc.baka9.catseedlogin.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import org.slf4j.Logger;

import java.nio.file.Path;

@Getter
@Plugin(
        id = "catseedlogin",
        name = "CatSeedLogin",
        version = "1.0.0", // 请确保在构建时替换为实际版本号
        authors = {"CatSeed"},
        description = "A login plugin for Velocity"
)
public class PluginMain {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    @Inject
    public PluginMain(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        // 加载配置
        Config.load(this);

        // 在构造函数中添加日志，确认插件被实例化
        logger.info("CatSeedLogin 插件构造函数被调用");
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        logger.info("CatSeedLogin 插件正在初始化...");

        // 注册事件监听器
        server.getEventManager().register(this, new Listeners(this));
        logger.info("事件监听器已注册");

        // 注册命令
        CommandManager commandManager = server.getCommandManager();
        commandManager.register(
                commandManager.metaBuilder("catseedloginbungee")
                        .aliases("cslb")
                        .build(),
                new Commands(this)
        );
        logger.info("命令已注册");
    }

    public void runAsync(Runnable runnable) {
        server.getScheduler().buildTask(this, runnable).schedule();
    }
}
