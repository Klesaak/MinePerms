package ua.klesaak.mineperms.velocity;

import ua.klesaak.mineperms.MinePermsManager;

public class MPVelocityListener {
    private final MinePermsManager minePermsManager;

    public MPVelocityListener(MinePermsVelocity plugin) {
        this.minePermsManager = plugin.getMinePermsManager();
        plugin.getServer().getEventManager().register(plugin, this);
    }
}
