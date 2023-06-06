package ua.klesaak.mineperms.bukkit;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import ua.klesaak.mineperms.manager.command.BukkitCommandSource;
import ua.klesaak.mineperms.manager.command.MinePermsCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//"§f[§cPermissionsEx§f] version [§91.23.4§f]"

public class MPBukkitCommand implements CommandExecutor, TabCompleter {
    private final MinePermsCommand minePermsCommand;

    public MPBukkitCommand(MinePermsBukkit plugin) {
        this.minePermsCommand = plugin.getMinePermsManager().getMinePermsCommand();
        Objects.requireNonNull(plugin.getCommand("mineperms")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("mineperms")).setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        this.minePermsCommand.invoke(new BukkitCommandSource(sender), label, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        List<String> onlinePlayers = new ArrayList<>(Bukkit.getMaxPlayers());
        Bukkit.getOnlinePlayers().forEach(player -> onlinePlayers.add(player.getName()));
        return this.minePermsCommand.onTabComplete(onlinePlayers, args);
    }

}

