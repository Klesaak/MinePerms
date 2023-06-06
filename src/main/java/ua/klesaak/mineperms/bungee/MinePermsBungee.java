package ua.klesaak.mineperms.bungee;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.Plugin;
import ua.klesaak.mineperms.MinePermsManager;
import ua.klesaak.mineperms.manager.event.bungee.BungeeEventManager;
import ua.klesaak.mineperms.manager.utils.Platform;

@Getter
public class MinePermsBungee extends Plugin {
    private volatile MinePermsManager minePermsManager;

    @Override
    public void onEnable() {
        long time = System.currentTimeMillis();
        this.minePermsManager = new MinePermsManager(Platform.BUNGEECORD);
        this.minePermsManager.loadConfig(this.getDataFolder());
        this.minePermsManager.initStorage();
        new MPBungeeCommand(this);
        new MPBungeeListener(this);
        this.minePermsManager.registerEventsManager(new BungeeEventManager());
        this.getLogger().info(ChatColor.GREEN + "Plugin successfully loaded (" + (System.currentTimeMillis() - time) + "ms) ");
    }

    @Override
    public void onDisable() {
        this.minePermsManager.getStorage().close();
    }
}
