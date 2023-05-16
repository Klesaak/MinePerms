package ua.klesaak.mineperms.manager.event;

import ua.klesaak.mineperms.manager.storage.User;

public interface MPEventManager {
    void callGroupChangeEvent(User user);
}
