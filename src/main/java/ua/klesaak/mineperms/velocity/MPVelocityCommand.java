package ua.klesaak.mineperms.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import lombok.val;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class MPVelocityCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        // Get the arguments after the command alias
        String[] args = invocation.arguments();
        if (source instanceof Player) {
            val velocityPlayer = (Player)source;
            val sourceName= velocityPlayer.getUsername();
            source.sendMessage(Component.text("Hello World " + sourceName).color(NamedTextColor.GOLD));
            return;
        }
        source.sendMessage(Component.text("Hello World!").color(NamedTextColor.AQUA));
    }

    // This method allows you to control who can execute the command.
    // If the executor does not have the required permission,
    // the execution of the command and the control of its autocompletion
    // will be sent directly to the server on which the sender is located
    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("command.test");
    }

    // With this method you can control the suggestions to send
    // to the CommandSource according to the arguments
    // it has already written or other requirements you need
    @Override
    public List<String> suggest(Invocation invocation) {
        return Collections.emptyList();
    }

    // Here you can offer argument suggestions in the same way as the previous method,
    // but asynchronously. It is recommended to use this method instead of the previous one
    // especially in cases where you make a more extensive logic to provide the suggestions
    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }
}
