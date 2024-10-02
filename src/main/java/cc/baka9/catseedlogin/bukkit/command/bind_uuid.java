package cc.baka9.catseedlogin.bukkit.command;

import cc.baka9.catseedlogin.bukkit.CatSeedLogin;
import cc.baka9.catseedlogin.bukkit.Config;
import cc.baka9.catseedlogin.bukkit.database.Cache;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayer;
import net.md_5.bungee.protocol.packet.Chat;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class bind_uuid implements CommandExecutor, TabCompleter {
    Plugin plugin = CatSeedLogin.getProvidingPlugin(CatSeedLogin.class);
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length==0){
            sender.sendMessage(ChatColor.RED+"请输入你的uuid");
            return false;
        }
        String uuid = args[0];
        File path = plugin.getDataFolder();
        File file = new File(path,"bind_uuid.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<String> allowed_uuid = config.getStringList("allowed_uuid");
        String sender_name = sender.getName();
        LoginPlayer lp = Cache.getIgnoreCase(sender_name);

        if(lp.getEmail() != null){
            sender.sendMessage(ChatColor.RED+"你已经绑定邮箱了");
            return false;
        }
        if(config.getString("confined_player."+sender_name)!=null){
            sender.sendMessage(ChatColor.RED+"你已经绑定过uuid了");
            return false;
        }
        if(!allowed_uuid.contains(uuid)){
            sender.sendMessage(ChatColor.RED+"该uuid未经授权");
            plugin.getLogger().warning(sender_name+"使用未经授权的uuid");
            return false;
        }
        List<String> players = config.getStringList("uuid_to_player."+uuid);
        int count = players.size();
        if (count >= Config.EmailVerify.MaxAccountsPerEmail) {
            sender.sendMessage("§c该uuid的账号数量已达到上限 (" + Config.EmailVerify.MaxAccountsPerEmail + ")，无法绑定新账号!");
            plugin.getLogger().warning(sender_name+"绑定uuid达到上限");
            return true;
        }
        players.add(sender_name);
        config.set("uuid_to_player."+uuid,players);
        config.set("confined_player."+sender_name,uuid);
        try {
            config.save(file);
            sender.sendMessage(ChatColor.GREEN+"绑定成功,现在你可以登录了");
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED+"绑定失败，请联系管理员");
            throw new RuntimeException(e);
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of("输入你的uuid");
    }
}
