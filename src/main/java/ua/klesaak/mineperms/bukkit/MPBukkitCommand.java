package ua.klesaak.mineperms.bukkit;

import lombok.NonNull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Collections;
import java.util.List;

public class MPBukkitCommand implements CommandExecutor, TabCompleter {
    private final MinePermsBukkit manager;

    public MPBukkitCommand(MinePermsBukkit manager) {
        this.manager = manager;
        this.manager.getCommand("mineperms").setExecutor(this);
        this.manager.getCommand("mineperms").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        sender.sendMessage(MinePermsBukkit.getMinePermsManager().getMinePermsCommand().invoke(args));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {

        return Collections.emptyList();
    }

}

