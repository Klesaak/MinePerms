package ua.klesaak.mineperms.manager.event.bukkit;

import ua.klesaak.mineperms.manager.event.IMPEventManager;
import ua.klesaak.mineperms.manager.storage.User;

public class BukkitEventManager implements IMPEventManager {

    @Override
    public void callGroupChangeEvent(User user) {
        new GroupChangeEventBukkit(user).call();
    }
}
