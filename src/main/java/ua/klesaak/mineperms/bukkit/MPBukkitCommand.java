package ua.klesaak.mineperms.bukkit;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

//"§f[§cPermissionsEx§f] version [§91.23.4§f]"

public class MPBukkitCommand implements CommandExecutor, TabCompleter {
    private final MinePermsBukkit plugin;

    public MPBukkitCommand(MinePermsBukkit plugin) {
        this.plugin = plugin;
        plugin.getCommand("mineperms").setExecutor(this);
        plugin.getCommand("mineperms").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        /* if (sender instanceof Player) {
            sender.sendMessage("§cOnly from console!");
            return true;
        }*/
        sender.sendMessage(this.plugin.getMinePermsManager().getMinePermsCommand().invoke(label, args));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        List<String> onlinePlayers = new ArrayList<>(Bukkit.getMaxPlayers());
        for (Player player : Bukkit.getOnlinePlayers()) {
            onlinePlayers.add(player.getName());
        }
        return this.plugin.getMinePermsManager().getMinePermsCommand().onTabComplete(label, onlinePlayers, args);
    }

}

