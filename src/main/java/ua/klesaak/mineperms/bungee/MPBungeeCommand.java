package ua.klesaak.mineperms.bungee;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import ua.klesaak.mineperms.manager.command.BungeeCommandSource;
import ua.klesaak.mineperms.manager.command.MinePermsCommand;

import java.util.ArrayList;
import java.util.List;

public class MPBungeeCommand extends Command implements TabExecutor {
    private final MinePermsCommand minePermsCommand;

    public MPBungeeCommand(MinePermsBungee plugin) {
        super("bmineperms", MinePermsCommand.MAIN_PERMISSION, "bmp", "bmperms", "bperms");
        this.minePermsCommand = plugin.getMinePermsManager().getMinePermsCommand();
        plugin.getProxy().getPluginManager().registerCommand(plugin, this);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        this.minePermsCommand.invoke(new BungeeCommandSource(sender), this.getName(), args);
    }


    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] args) {
        List<String> onlinePlayers = new ArrayList<>();
        ProxyServer.getInstance().getPlayers().forEach(pp -> onlinePlayers.add(pp.getName()));
        return this.minePermsCommand.onTabComplete(onlinePlayers, args);
    }
}
