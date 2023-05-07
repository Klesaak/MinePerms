package ua.klesaak.mineperms.bungee;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import ua.klesaak.mineperms.manager.MinePermsCommand;
import ua.klesaak.mineperms.manager.utils.UtilityMethods;

import java.util.ArrayList;
import java.util.Collections;

public class MPBungeeCommand extends Command implements TabExecutor {
    private final MinePermsBungee manager;

    public MPBungeeCommand(MinePermsBungee manager) {
        super("bmineperms", MinePermsCommand.MAIN_PERMISSION, "bmp", "bmperms", "bperms");
        this.manager = manager;
        manager.getProxy().getPluginManager().registerCommand(manager, this);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(TextComponent.fromLegacyText(this.manager.getMinePermsManager().getMinePermsCommand().invoke(this.getName(), args)));
    }


    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] args) {
        if (args.length == 1) {
            return UtilityMethods.copyPartialMatches(args[0].toLowerCase(), MinePermsCommand.SUB_COMMANDS_0, new ArrayList<>());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("user")) {
            return UtilityMethods.copyPartialMatches(args[1].toLowerCase(), MinePermsCommand.USER_SUB_COMMANDS_0, new ArrayList<>());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("group")) {
            return UtilityMethods.copyPartialMatches(args[1].toLowerCase(), MinePermsCommand.GROUP_SUB_COMMANDS_0, new ArrayList<>());
        }
        return Collections.emptyList();
    }
}
