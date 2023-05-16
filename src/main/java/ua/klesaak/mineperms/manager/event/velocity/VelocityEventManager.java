package ua.klesaak.mineperms.manager.event.velocity;

import ua.klesaak.mineperms.manager.event.IMPEventManager;
import ua.klesaak.mineperms.manager.storage.User;
import ua.klesaak.mineperms.velocity.MinePermsVelocity;

public class VelocityEventManager implements IMPEventManager {
    private final MinePermsVelocity plugin;

    public VelocityEventManager(MinePermsVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void callGroupChangeEvent(User user) {
        this.plugin.getServer().getEventManager().fire(new GroupChangeEventVelocity(user));
    }
}
