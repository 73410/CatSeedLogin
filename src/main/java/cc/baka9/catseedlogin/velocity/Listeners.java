package cc.baka9.catseedlogin.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Listeners {

    private final PluginMain plugin;
    private final Communication communication;
    private final Set<String> loggedInPlayerSet = ConcurrentHashMap.newKeySet();

    public Listeners(PluginMain plugin) {
        this.plugin = plugin;
        this.communication = new Communication(plugin.getLogger());
    }

    /**
     * 登录之前不能输入代理指令
     */
    @Subscribe
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (!message.startsWith("/")) return;

        boolean loggedIn = loggedInPlayerSet.contains(player.getUsername());

        if (!loggedIn) {
            event.setResult(PlayerChatEvent.ChatResult.denied());

            // 向玩家发送未登录的提示信息
            player.sendMessage(Component.text("您尚未登录，无法执行命令。"));

            plugin.runAsync(() -> {
                try {
                    int result = communication.sendConnectRequest(player.getUsername());
                    if (result == 1) {
                        // 在主线程中更新状态并执行命令
                        plugin.getServer().getScheduler().buildTask(plugin, () -> {
                            loggedInPlayerSet.add(player.getUsername());
                            plugin.getServer().getCommandManager().executeImmediatelyAsync(player, message.substring(1));
                        }).schedule();
                    }
                } catch (Exception e) {
                    plugin.getLogger().error("异步任务中发生异常", e);
                }
            });
        }
    }

    /**
     * 玩家切换子服时，检查代理端该玩家的登录状态
     */
    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getUsername();

        // 获取目标服务器的名称
        Optional<RegisteredServer> targetServerOptional = event.getResult().getServer();
        if (!targetServerOptional.isPresent()) {
            return; // 如果没有目标服务器，直接返回
        }
        String targetServerName = targetServerOptional.get().getServerInfo().getName();

        // 如果目标服务器是登录服务器，允许切换
        if (targetServerName.equals(Config.LoginServerName)) {
            return;
        }

        boolean loggedIn = loggedInPlayerSet.contains(playerName);

        if (!loggedIn) {
            // 检查玩家当前所在的服务器
            Optional<ServerConnection> currentServerOptional = player.getCurrentServer();
            if (currentServerOptional.isPresent()) {
                // 玩家在服务器中，可能是主动切换服务器
                // 阻止服务器切换
                event.setResult(ServerPreConnectEvent.ServerResult.denied());

                // 向玩家发送未登录的提示信息
                player.sendMessage(Component.text("您尚未登录，无法切换服务器。"));

                // 异步检查玩家的登录状态
                plugin.runAsync(() -> {
                    try {
                        int result = communication.sendConnectRequest(playerName);
                        if (result == 1) {
                            // 在主线程中更新玩家状态并执行服务器切换
                            plugin.getServer().getScheduler().buildTask(plugin, () -> {
                                if (!player.isActive()) {
                                    plugin.getLogger().info("玩家已离线，停止操作：" + playerName);
                                    return; // 玩家已离线，停止操作
                                }
                                loggedInPlayerSet.add(playerName);
                                // 重新尝试连接到目标服务器
                                Optional<RegisteredServer> serverOptional = plugin.getServer().getServer(targetServerName);
                                if (serverOptional.isPresent()) {
                                    player.createConnectionRequest(serverOptional.get()).fireAndForget();
                                } else {
                                    player.sendMessage(Component.text("目标服务器不可用。"));
                                }
                            }).schedule();
                        }
                    } catch (Exception e) {
                        plugin.getLogger().error("异步任务中发生异常", e);
                    }
                });
            }
        }
    }


    /**
     * 玩家连接到服务器后，如果是登录服务器，更新子服的登录状态
     */
    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        String serverName = event.getServer().getServerInfo().getName();
        if (serverName.equals(Config.LoginServerName)) {
            Player player = event.getPlayer();
            String playerName = player.getUsername();

            plugin.runAsync(() -> {
                try {
                    boolean loggedIn = loggedInPlayerSet.contains(playerName);
                    if (loggedIn) {
                        communication.sendKeepLoggedInRequest(playerName);
                    }
                } catch (Exception e) {
                    plugin.getLogger().error("异步任务中发生异常", e);
                }
            });
        }
    }

    /**
     * 玩家断开连接时，移除登录状态
     */
    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().buildTask(plugin, () -> {
            plugin.getLogger().info("玩家断开连接：" + player.getUsername());
            loggedInPlayerSet.remove(player.getUsername());
        }).schedule();
    }

    /**
     * 玩家在登录之前，检查代理端和子服的登录状态
     */
    @Subscribe
    public void onPreLogin(PreLoginEvent event) {
        String playerName = event.getUsername();
        boolean loggedIn = loggedInPlayerSet.contains(playerName);

        plugin.getLogger().info("玩家尝试登录：" + playerName + "，已登录状态：" + loggedIn);

        int connectRequestResult = communication.sendConnectRequest(playerName);
        plugin.getLogger().info("通信结果：" + connectRequestResult);

        if (loggedIn || connectRequestResult == 1) {
            plugin.getLogger().info("拒绝玩家连接：" + playerName);
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text("您已在服务器中登录，不能重复登录。")));
        }
    }
}