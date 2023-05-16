package ua.klesaak.mineperms.manager.event.bukkit;

import ua.klesaak.mineperms.manager.event.MPEventManager;
import ua.klesaak.mineperms.manager.storage.User;

public class BukkitEventManager implements MPEventManager {

    @Override
    public void callGroupChangeEvent(User user) {
        new GroupChangeEventBukkit(user).call();
    }
}
