package ua.klesaak.mineperms.bukkit;

import lombok.NonNull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ua.klesaak.mineperms.manager.MinePermsCommand;
import ua.klesaak.mineperms.manager.utils.UtilityMethods;

import java.util.ArrayList;
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
        if (sender instanceof Player) {
            sender.sendMessage("§cOnly from console!");
            return true;
        }
        sender.sendMessage(this.plugin.getMinePermsManager().getMinePermsCommand().invoke(args));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (args.length == 1) {
            return UtilityMethods.copyPartialMatches(args[0].toLowerCase(), MinePermsCommand.SUB_COMMANDS_0, new ArrayList<>());
        }
        if (args.length == 3 && args[2].equalsIgnoreCase("user")) {
            return UtilityMethods.copyPartialMatches(args[2].toLowerCase(), MinePermsCommand.USER_SUB_COMMANDS_0, new ArrayList<>());
        }

        return Collections.emptyList();
    }

}

