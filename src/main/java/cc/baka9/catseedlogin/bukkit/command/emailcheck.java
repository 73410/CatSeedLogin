package cc.baka9.catseedlogin.bukkit.command;

import cc.baka9.catseedlogin.bukkit.CatSeedLogin;
import cc.baka9.catseedlogin.bukkit.database.Cache;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayer;
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
import java.util.ArrayList;
import java.util.List;

public class emailcheck implements CommandExecutor, TabCompleter {
    Plugin plugin = CatSeedLogin.getProvidingPlugin(CatSeedLogin.class);
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length<2){
            sender.sendMessage(ChatColor.RED+"缺少参数");
            return false;
        }
        if(!sender.hasPermission("catseedlogin.command.adduuid")){
            sender.sendMessage(ChatColor.RED+"你没有权限这么做");
            return false;
        }
        switch (args[0]) {
            case "playeremail":
                String targetPlayerName = args[1];
                LoginPlayer targetPlayer = Cache.getIgnoreCase(targetPlayerName);

                if (targetPlayer == null || targetPlayer.getEmail() == null) {
                    sender.sendMessage("§c玩家 " + targetPlayerName + " 未绑定邮箱或未注册!");
                } else {
                    sender.sendMessage("§6玩家 " + targetPlayerName + " 绑定的邮箱为: " + targetPlayer.getEmail());
                }
                return true;
            case "emailusers":
                String email = args[1];
                List<String> playerNames = new ArrayList<>();

                synchronized (Cache.getAllLoginPlayer()) {
                    for (LoginPlayer player : Cache.getAllLoginPlayer()) {
                        if (email.equalsIgnoreCase(player.getEmail())) {
                            playerNames.add(player.getName());
                        }
                    }
                }

                if (playerNames.isEmpty()) {
                    sender.sendMessage("§c没有玩家绑定此邮箱: " + email);
                } else {
                    sender.sendMessage("§6绑定此邮箱的玩家有: " + String.join(", ", playerNames));
                }
                break;
            case "uuidusers":
                String name = args[1];
                File path = plugin.getDataFolder();
                File file = new File(path, "bind_uuid.yml");
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                String uuid = config.getString("confined_player." + name);
                List<String> list = config.getStringList("uuid_to_player." + uuid);
                if (list.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + "该玩家未使用uuid绑定");
                } else {
                    sender.sendMessage(list.toString());
                }
                break;
            case "player-players":
                String PlayerName = args[1];
                LoginPlayer Player = Cache.getIgnoreCase(PlayerName);
                String emails;
                if (Player == null || Player.getEmail() == null) {
                    sender.sendMessage("§c玩家 " + PlayerName + " 未绑定邮箱或未注册!");
                    return false;
                } else {
                  emails = Player.getEmail();
                }
                List<String> Names = new ArrayList<>();

                synchronized (Cache.getAllLoginPlayer()) {
                    for (LoginPlayer player : Cache.getAllLoginPlayer()) {
                        if (emails.equalsIgnoreCase(player.getEmail())) {
                            Names.add(player.getName());
                        }
                    }
                }

                if (Names.isEmpty()) {
                    sender.sendMessage("§c没有玩家绑定此邮箱: " + emails);
                } else {
                    sender.sendMessage("§6绑定此邮箱的玩家有: " + String.join(", ", Names));
                }
                break;
        }

        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length==1){
            return List.of("playeremail","emailusers","uuidusers","player-players");
        }
        return null;
    }
}
