package ua.klesaak.mineperms.bukkit;

import lombok.NonNull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Collections;
import java.util.List;

public class MPBukkitCommand implements CommandExecutor, TabCompleter {
    private final MinePermsBukkit plugin;

    public MPBukkitCommand(MinePermsBukkit plugin) {
        this.plugin = plugin;
        plugin.getCommand("mineperms").setExecutor(this);
        plugin.getCommand("mineperms").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        sender.sendMessage(this.plugin.getMinePermsManager().getMinePermsCommand().invoke(args));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {

        return Collections.emptyList();
    }

}

