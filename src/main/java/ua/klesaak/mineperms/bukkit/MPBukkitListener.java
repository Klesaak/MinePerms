package ua.klesaak.mineperms.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ua.klesaak.mineperms.bukkit.integration.PermissibleOverride;
import ua.klesaak.mineperms.manager.storage.Storage;

import java.util.concurrent.CompletableFuture;

public class MPBukkitListener implements Listener {
    private final Storage storage;

    public MPBukkitListener(MinePermsBukkit plugin) {
        this.storage = plugin.getMinePerms().getStorage();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onLogin(PlayerLoginEvent event) {
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) return;
        Player player = event.getPlayer();
        String playerName = player.getName();
        this.storage.cacheUser(playerName);
        CompletableFuture.runAsync(()-> PermissibleOverride.injectPlayer(player, new PermissibleOverride(playerName, this.storage))).exceptionally(throwable -> {
            throw new RuntimeException("Error while inject player ", throwable);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public final void onJoin(PlayerJoinEvent event) {
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLeave(PlayerQuitEvent event) {
        this.storage.unCacheUser(event.getPlayer().getName());
    }
}
