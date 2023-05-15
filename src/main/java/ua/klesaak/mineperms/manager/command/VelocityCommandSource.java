package ua.klesaak.mineperms.manager.command;

import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;

public class VelocityCommandSource implements IMPCommandSource {
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
