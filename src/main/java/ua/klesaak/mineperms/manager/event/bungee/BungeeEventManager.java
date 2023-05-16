package ua.klesaak.mineperms.manager.event.bungee;

import ua.klesaak.mineperms.manager.event.MPEventManager;
import ua.klesaak.mineperms.manager.storage.User;

public class BungeeEventManager implements MPEventManager {

    @Override
    public void callGroupChangeEvent(User user) {
        new GroupChangeEventBungee(user).call();
    }
}
