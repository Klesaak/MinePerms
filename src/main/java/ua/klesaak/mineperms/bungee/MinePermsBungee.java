package ua.klesaak.mineperms.bungee;

import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;
import ua.klesaak.mineperms.MinePermsManager;

@Getter
public class MinePermsBungee extends Plugin {
    private volatile MinePermsManager minePermsManager;
    @Override
    public void onEnable() {
        this.minePermsManager = new MinePermsManager();
        this.minePermsManager.loadConfig(this.getDataFolder());
        this.minePermsManager.initStorage();
        new MPBungeeCommand(this);
        new MPBungeeListener(this);
    }

    @Override
    public void onDisable() {
        this.minePermsManager.getStorage().close();
    }
}
