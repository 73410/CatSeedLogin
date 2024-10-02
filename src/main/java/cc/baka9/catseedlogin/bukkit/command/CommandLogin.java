package cc.baka9.catseedlogin.bukkit.command;

import cc.baka9.catseedlogin.bukkit.CatSeedLogin;
import cc.baka9.catseedlogin.bukkit.Config;
import cc.baka9.catseedlogin.bukkit.database.Cache;
import cc.baka9.catseedlogin.bukkit.event.CatSeedPlayerLoginEvent;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayer;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import cc.baka9.catseedlogin.util.Crypt;
import cc.baka9.catseedlogin.util.Util;
import net.md_5.bungee.protocol.packet.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class CommandLogin implements CommandExecutor {
    Plugin plugin = CatSeedLogin.getProvidingPlugin(CatSeedLogin.class);
    File path = plugin.getDataFolder();
    @Override
    public boolean onCommand(CommandSender sender, Command command, String lable, String[] args){
        File file = new File(path,"bind_uuid.yml");
        File re = new File(path, "reqw.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        FileConfiguration repw = YamlConfiguration.loadConfiguration(re);

        if (args.length == 0 || !(sender instanceof Player)) return false;
        Player player = (Player) sender;
        String name = player.getName();
        if (LoginPlayerHelper.isLogin(name)) {
            sender.sendMessage(Config.Language.LOGIN_REPEAT);
            return true;
        }
        LoginPlayer lp = Cache.getIgnoreCase(name);
        if (lp == null) {
            sender.sendMessage(Config.Language.LOGIN_NOREGISTER);
            return true;
        }
        if (Objects.equals(Crypt.encrypt(name, args[0]), lp.getPassword().trim())) {
            // 检查是否已经绑定了邮箱
            if (lp.getEmail() == null || !Util.checkMail(lp.getEmail())) {
                if(config.getString("confined_player."+name)==null){
                    sender.sendMessage("§c你还没有绑定邮箱或者uuid");
                    sender.sendMessage(ChatColor.YELLOW+"使用 /bdmail set <你的邮箱> 绑定邮箱.");
                    sender.sendMessage(ChatColor.AQUA+"没有邮箱？使用 /bdid <uuid> 绑定uuid.（uuid在qq联系管理员获取）");
                    plugin.getLogger().info(name+"未绑定id或者邮箱");
                    return true;
                }
                sender.sendMessage(ChatColor.AQUA+"你已绑定uuid，请妥善保管uuid，否则后果自负");
            }
            LoginPlayerHelper.add(lp);
            CatSeedPlayerLoginEvent loginEvent = new CatSeedPlayerLoginEvent(player, lp.getEmail(), CatSeedPlayerLoginEvent.Result.SUCCESS);
            Bukkit.getServer().getPluginManager().callEvent(loginEvent);
            sender.sendMessage(Config.Language.LOGIN_SUCCESS);
            player.updateInventory();
            LoginPlayerHelper.recordCurrentIP(player, lp);
            if (Config.Settings.AfterLoginBack && Config.Settings.CanTpSpawnLocation) {
                Config.getOfflineLocation(player).ifPresent(player::teleport);
            }
        } else {
            sender.sendMessage(Config.Language.LOGIN_FAIL);
            CatSeedPlayerLoginEvent loginEvent = new CatSeedPlayerLoginEvent(player, lp.getEmail(), CatSeedPlayerLoginEvent.Result.FAIL);
            Bukkit.getServer().getPluginManager().callEvent(loginEvent);
            if (Config.EmailVerify.Enable) {
                sender.sendMessage(Config.Language.LOGIN_FAIL_IF_FORGET);
            }
        }
        if(!repw.getBoolean(name)){
            repw.set(name,true);
            try {
                repw.save(re);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }
}
