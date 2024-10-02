package cc.baka9.catseedlogin.bukkit.command;

import cc.baka9.catseedlogin.bukkit.CatSeedLogin;
import cc.baka9.catseedlogin.bukkit.Config;
import cc.baka9.catseedlogin.bukkit.database.Cache;
import cc.baka9.catseedlogin.bukkit.event.CatSeedPlayerRegisterEvent;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayer;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import cc.baka9.catseedlogin.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class CommandRegister implements CommandExecutor {
    Plugin plugin = CatSeedLogin.getProvidingPlugin(CatSeedLogin.class);
    @Override
    public boolean onCommand(CommandSender sender, Command command, String lable, String[] args){
        if (args.length < 2){
            sender.sendMessage(ChatColor.RED+"输入密码后空一格需要再输入一次！");
            return false;
        }
        Player player = (Player) sender;
        String name = sender.getName();
        if (LoginPlayerHelper.isLogin(name)) {
            sender.sendMessage(Config.Language.REGISTER_AFTER_LOGIN_ALREADY);
            return true;
        }
        if (LoginPlayerHelper.isRegister(name)) {
            sender.sendMessage(Config.Language.REGISTER_BEFORE_LOGIN_ALREADY);
            return true;
        }
        if (!args[0].equals(args[1])) {
            sender.sendMessage(Config.Language.REGISTER_PASSWORD_CONFIRM_FAIL);
            return true;
        }
        if (!Util.passwordIsDifficulty(args[0])) {
            sender.sendMessage(Config.Language.COMMON_PASSWORD_SO_SIMPLE);
            return true;
        }
        if (!Cache.isLoaded) {
            return true;
        }
        sender.sendMessage("§e注册中..");
        CatSeedLogin.instance.runTaskAsync(() -> {
            try {
                String currentIp = player.getAddress().getAddress().getHostAddress();
                List<LoginPlayer> LoginPlayerListlikeByIp = CatSeedLogin.sql.getLikeByIp(currentIp);
                if (LoginPlayerListlikeByIp.size() >= Config.Settings.IpRegisterCountLimit && !Config.Settings.IpWhitelist.contains(currentIp)) {
                    sender.sendMessage(Config.Language.REGISTER_MORE
                            .replace("{count}", String.valueOf(LoginPlayerListlikeByIp.size()))
                            .replace("{accounts}", String.join(", ", LoginPlayerListlikeByIp.stream().map(LoginPlayer::getName).toArray(String[]::new))));
                    plugin.getLogger().warning(sender.getName()+"注册过多账号");
                } else {
                    LoginPlayer lp = new LoginPlayer(name, args[0]);
                    lp.crypt();
                    CatSeedLogin.sql.add(lp);
                    //LoginPlayerHelper.add(lp);
                    Bukkit.getScheduler().runTask(CatSeedLogin.instance, () -> {
                        CatSeedPlayerRegisterEvent event = new CatSeedPlayerRegisterEvent(Bukkit.getPlayer(sender.getName()));
                        Bukkit.getServer().getPluginManager().callEvent(event);
                    });
                    sender.sendMessage(Config.Language.REGISTER_SUCCESS);
                    //player.updateInventory();
                    //LoginPlayerHelper.recordCurrentIP(player, lp);
                    sender.sendMessage(ChatColor.GREEN+"请使用/l 密码 进行登录");
                }


            } catch (Exception e) {
                e.printStackTrace();
                sender.sendMessage("§c服务器内部错误!");
            }
        });
        return true;

    }
}
