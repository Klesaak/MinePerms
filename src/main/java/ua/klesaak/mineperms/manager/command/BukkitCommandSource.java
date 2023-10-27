package ua.klesaak.mineperms.manager.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class BukkitCommandSource implements IMPCommandSource {
    private final CommandSender sender;

    public BukkitCommandSource(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.sender.hasPermission(permission);
    }

    @Override
    public void sendMessage(String message) {
        this.sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}
