package ua.klesaak.mineperms.bungee;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PermissionCheckEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.Objects;

public class MPBungeeListener implements Listener {
    private final MinePermsBungee plugin;

    public MPBungeeListener(MinePermsBungee plugin) {
        this.plugin = plugin;
        plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPermissionCheck(PermissionCheckEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer)) return;
        Objects.requireNonNull(event.getPermission(), "permission");
        Objects.requireNonNull(event.getSender(), "sender");
        String permission = event.getPermission();
        event.setHasPermission(this.plugin.getMinePermsManager().hasPermission(event.getSender().getName(), permission));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerLogin(LoginEvent event) {
        this.plugin.getMinePermsManager().getStorage().cacheUser(event.getConnection().getName());
    }

    @EventHandler
    public void onPlayerPostLogin(PostLoginEvent event) {
    }

    // Подождите, пока выгрузится последний приоритет, чтобы плагины все еще могли выполнять проверки разрешений для этого события.
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerDisconnectEvent event) {
        this.plugin.getMinePermsManager().getStorage().unCacheUser(event.getPlayer().getName());
    }
}
