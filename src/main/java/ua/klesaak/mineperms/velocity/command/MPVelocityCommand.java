package ua.klesaak.mineperms.velocity.command;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import lombok.val;
import ua.klesaak.mineperms.manager.command.MinePermsCommand;
import ua.klesaak.mineperms.velocity.MinePermsVelocity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class MPVelocityCommand implements SimpleCommand {
    private final MinePermsVelocity minePermsVelocity;
    private final MinePermsCommand minePermsCommand;

    public MPVelocityCommand(MinePermsVelocity minePermsVelocity) {
        this.minePermsVelocity = minePermsVelocity;
        this.minePermsCommand = minePermsVelocity.getMinePermsManager().getMinePermsCommand();
        CommandManager commandManager = minePermsVelocity.getServer().getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("vmineperms")
                .aliases("vmp", "vmperms", "vperms")
                .plugin(minePermsVelocity)
                .build();
        commandManager.register(commandMeta, this);
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        this.minePermsCommand.invoke(new VelocityCommandSource(source), invocation.alias(), args);
    }


    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(MinePermsCommand.MAIN_PERMISSION);
    }


    @Override
    public List<String> suggest(Invocation invocation) {
        return Collections.emptyList();
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        List<String> onlinePlayers = new ArrayList<>();
        for (val player : this.minePermsVelocity.getServer().getAllPlayers()) {
            onlinePlayers.add(player.getUsername());
        };
        return CompletableFuture.completedFuture(this.minePermsCommand.onTabComplete(invocation.alias(), onlinePlayers, invocation.arguments()));
    }
}
