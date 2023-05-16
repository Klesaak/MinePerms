package ua.klesaak.mineperms.manager.event.bungee;

import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Event;
import ua.klesaak.mineperms.manager.storage.User;

@Getter
public class GroupChangeEventBungee extends Event {
    private final User user;

    public GroupChangeEventBungee(User user) {
        this.user = user;
    }

    public void call() {
        ProxyServer.getInstance().getPluginManager().callEvent(this);
    }
}
