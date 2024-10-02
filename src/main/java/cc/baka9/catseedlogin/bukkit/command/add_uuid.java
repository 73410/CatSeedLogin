package cc.baka9.catseedlogin.bukkit.command;

import cc.baka9.catseedlogin.bukkit.CatSeedLogin;
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

public class add_uuid implements CommandExecutor, TabCompleter {
    Plugin plugin = CatSeedLogin.getProvidingPlugin(CatSeedLogin.class);
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!sender.hasPermission("catseedlogin.command.adduuid")){
            sender.sendMessage(ChatColor.RED+"你没有权限这么做");
            return false;
        }
        if(args.length==0){
            sender.sendMessage(ChatColor.RED+"请输入uuid");
            sender.sendMessage(ChatColor.AQUA+"获取uuid方法看管理群");
            return false;
        }
        String uuid = args[0];
        File path = plugin.getDataFolder();
        File file = new File(path,"bind_uuid.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<String> allowed_uuid = config.getStringList("allowed_uuid");

        if(allowed_uuid.contains(uuid)){
            sender.sendMessage(ChatColor.RED+"已添加过此id");
            return false;
        }
        allowed_uuid.add(uuid);
        config.set("allowed_uuid",allowed_uuid);
        try {
            config.save(file);
            sender.sendMessage(ChatColor.GREEN+"添加成功");
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED+"添加失败");
            throw new RuntimeException(e);
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of("输入uuid");
    }
}
