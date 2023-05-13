package ua.klesaak.mineperms.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import ua.klesaak.mineperms.manager.command.MPCommandSource;

public class VelocityCommandSource implements MPCommandSource {
    private final CommandSource sender;

    public VelocityCommandSource(CommandSource sender) {
        this.sender = sender;
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.sender.hasPermission(permission);
    }

    @Override
    public void sendMessage(String message) {
        this.sender.sendMessage(Component.text(message));
    }
}
