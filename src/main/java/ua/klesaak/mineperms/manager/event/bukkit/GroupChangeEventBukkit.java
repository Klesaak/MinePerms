package ua.klesaak.mineperms.manager.event.bukkit;

import lombok.Getter;
import ua.klesaak.mineperms.manager.storage.User;

@Getter
public class GroupChangeEventBukkit extends AbstractBukkitEvent {
    private final User user;

    public GroupChangeEventBukkit(User user) {
        this.user = user;
    }
}
