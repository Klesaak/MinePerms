package ua.klesaak.mineperms.bungee;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.Collections;

public class MPBungeeCommand extends Command implements TabExecutor {
    private final MinePermsBungee manager;

    public MPBungeeCommand(MinePermsBungee manager) {
        super("bmineperms", "mineperms.admin", "bmp", "bmperms", "bperms", "perms");
        this.manager = manager;
        manager.getProxy().getPluginManager().registerCommand(manager, this);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }


    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] strings) {


        return Collections.emptyList();
    }
}
