package ua.klesaak.mineperms.manager.command;

import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class VelocityCommandSource implements IMPCommandSource {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand().toBuilder()
            .extractUrls(Style.style().decoration(TextDecoration.UNDERLINED, true).build()).build();
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
        this.sender.sendMessage(LEGACY_SERIALIZER.deserialize(message));
    }
}
