package ua.klesaak.mineperms.bukkit;

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
        this.plugin.getCommand("mineperms").setExecutor(this);
        this.plugin.getCommand("mineperms").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        return Collections.emptyList();
    }

}

