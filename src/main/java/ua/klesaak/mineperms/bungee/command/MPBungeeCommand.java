package ua.klesaak.mineperms.bungee.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import ua.klesaak.mineperms.bungee.MinePermsBungee;
import ua.klesaak.mineperms.manager.command.MinePermsCommand;

import java.util.ArrayList;
import java.util.List;

public class MPBungeeCommand extends Command implements TabExecutor {
    private final MinePermsBungee plugin;

    public MPBungeeCommand(MinePermsBungee plugin) {
        super("bmineperms", MinePermsCommand.MAIN_PERMISSION, "bmp", "bmperms", "bperms");
        this.plugin = plugin;
        plugin.getProxy().getPluginManager().registerCommand(plugin, this);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        this.plugin.getMinePermsManager().getMinePermsCommand().invoke(new BungeeCommandSource(sender), this.getName(), args);
    }


    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] args) {
        List<String> onlinePlayers = new ArrayList<>();
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            onlinePlayers.add(player.getName());
        }
        return this.plugin.getMinePermsManager().getMinePermsCommand().onTabComplete(this.getName(), onlinePlayers, args);
    }
}
