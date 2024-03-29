package ua.klesaak.mineperms.bungee;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PermissionCheckEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import ua.klesaak.mineperms.manager.storage.Storage;

import java.util.Objects;

public class MPBungeeListener implements Listener {
    private final Storage storage;

    public MPBungeeListener(MinePermsBungee plugin) {
        this.storage = plugin.getMinePerms().getStorage();
        plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPermissionCheck(PermissionCheckEvent event) {
        CommandSender sender = event.getSender();
        if (!(sender instanceof ProxiedPlayer)) return;
        Objects.requireNonNull(event.getPermission(), "permission");
        Objects.requireNonNull(event.getSender(), "sender");
        String permission = event.getPermission();
        event.setHasPermission(this.storage.hasPermission(sender.getName(), permission));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(LoginEvent event) {
        if (event.isCancelled()) return;
        this.storage.cacheUser(event.getConnection().getName());
    }

    // Подождите, пока выгрузится последний приоритет, чтобы плагины все еще могли выполнять проверки разрешений для этого события.
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerDisconnectEvent event) {
        this.storage.unCacheUser(event.getPlayer().getName());
    }
}
