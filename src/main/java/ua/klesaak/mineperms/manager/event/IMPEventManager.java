package ua.klesaak.mineperms.manager.event;

import ua.klesaak.mineperms.manager.storage.User;

public interface IMPEventManager {
    void callGroupChangeEvent(User user);
}
