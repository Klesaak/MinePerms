package ua.klesaak.mineperms.bungee;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import ua.klesaak.mineperms.MinePermsManager;

import java.util.Collections;

public class MPBungeeCommand extends Command implements TabExecutor {
    private final MinePermsBungee manager;

    public MPBungeeCommand(MinePermsBungee manager) {
        super("bmineperms", MinePermsManager.MAIN_PERMISSION, "bmp", "bmperms", "bperms");
        this.manager = manager;
        manager.getProxy().getPluginManager().registerCommand(manager, this);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(TextComponent.fromLegacyText(this.manager.getMinePermsManager().getMinePermsCommand().invoke(args)));
    }


    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] args) {


        return Collections.emptyList();
    }
}
