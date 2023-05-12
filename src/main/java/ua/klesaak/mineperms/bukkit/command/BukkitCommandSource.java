package ua.klesaak.mineperms.bukkit.command;

import org.bukkit.command.CommandSender;
import ua.klesaak.mineperms.manager.command.MPCommandSource;

public class BukkitCommandSource implements MPCommandSource {
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
        this.sender.sendMessage(message);
    }
}
