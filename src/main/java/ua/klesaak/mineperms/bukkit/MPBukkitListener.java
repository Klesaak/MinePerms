package ua.klesaak.mineperms.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ua.klesaak.mineperms.bukkit.integration.PermissibleOverride;

import java.util.concurrent.CompletableFuture;

public class MPBukkitListener implements Listener {
    private final MinePermsBukkit plugin;

    public MPBukkitListener(MinePermsBukkit plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public final void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        CompletableFuture.runAsync(()-> PermissibleOverride.injectPlayer(player, new PermissibleOverride(this.plugin.getMinePermsManager(), player)));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeave(PlayerQuitEvent event) {

    }
}
