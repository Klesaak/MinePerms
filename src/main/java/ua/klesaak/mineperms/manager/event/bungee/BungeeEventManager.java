package ua.klesaak.mineperms.manager.event.bungee;

import ua.klesaak.mineperms.manager.event.IMPEventManager;
import ua.klesaak.mineperms.manager.storage.User;

public class BungeeEventManager implements IMPEventManager {

    @Override
    public void callGroupChangeEvent(User user) {
        new GroupChangeEventBungee(user).call();
    }
}
