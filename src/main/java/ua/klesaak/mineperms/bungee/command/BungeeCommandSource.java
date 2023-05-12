package ua.klesaak.mineperms.bungee.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import ua.klesaak.mineperms.manager.command.MPCommandSource;

public class BungeeCommandSource implements MPCommandSource {
    private final CommandSender sender;

    public BungeeCommandSource(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.sender.hasPermission(permission);
    }

    @Override
    public void sendMessage(String message) {
        this.sender.sendMessage(TextComponent.fromLegacyText(message));
    }
}
