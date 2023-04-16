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
    private final MinePermsBungee manager;

    public MPBungeeListener(MinePermsBungee manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerPermissionCheck(PermissionCheckEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer)) {
            return;
        }
        Objects.requireNonNull(event.getPermission(), "permission");
        Objects.requireNonNull(event.getSender(), "sender");
        String permission = event.getPermission();
        ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        event.setHasPermission(this.manager.getMinePermsManager().hasPermission(player.getUniqueId(), permission));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onOtherPermissionCheck(PermissionCheckEvent event) {
        if (event.getSender() instanceof ProxiedPlayer) {
            return;
        }
        Objects.requireNonNull(event.getPermission(), "permission");
        Objects.requireNonNull(event.getSender(), "sender");
        String permission = event.getPermission();

    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerLogin(LoginEvent event) {

    }

    @EventHandler
    public void onPlayerPostLogin(PostLoginEvent event) {

    }

    // Wait until the last priority to unload, so plugins can still perform permission checks on this event
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerDisconnectEvent event) {
        //handleDisconnect(e.getPlayer().getUniqueId());
    }
}
