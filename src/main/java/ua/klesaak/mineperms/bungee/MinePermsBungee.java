package ua.klesaak.mineperms.bungee;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.Plugin;
import ua.klesaak.mineperms.MinePerms;
import ua.klesaak.mineperms.manager.event.bungee.BungeeEventManager;
import ua.klesaak.mineperms.manager.log.MPLogger;
import ua.klesaak.mineperms.manager.utils.Platform;

@Getter
public class MinePermsBungee extends Plugin {
    private MinePerms minePerms;

    @Override
    public void onEnable() {
        long time = System.currentTimeMillis();
        this.minePerms = new MinePerms(Platform.BUNGEECORD);
        this.minePerms.init(this.getDataFolder(), new BungeeEventManager());
        new MPBungeeCommand(this);
        new MPBungeeListener(this);
        MPLogger.register(this.getLogger());
        this.getLogger().info(ChatColor.GREEN + "Plugin successfully loaded (" + (System.currentTimeMillis() - time) + "ms) ");
    }

    @Override
    public void onDisable() {
        this.minePerms.getStorage().close();
    }
}
