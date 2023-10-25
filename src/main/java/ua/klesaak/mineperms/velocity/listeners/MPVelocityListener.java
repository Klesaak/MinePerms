package ua.klesaak.mineperms.velocity.listeners;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import ua.klesaak.mineperms.MinePermsManager;
import ua.klesaak.mineperms.velocity.MinePermsVelocity;

public class MPVelocityListener {
    private final MinePermsManager minePermsManager;

    public MPVelocityListener(MinePermsVelocity plugin) {
        this.minePermsManager = plugin.getMinePermsManager();
        plugin.getServer().getEventManager().register(plugin, this);
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerLogin(LoginEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(Component.text("My long messageeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"));
    }

    @Subscribe
    public void onPlayerPostLogin(LoginEvent event) {
        final Player player = event.getPlayer();

        if (!event.getResult().isAllowed()) return;
        this.minePermsManager.getStorage().cacheUser(player.getUsername());
    }

    // Wait until the last priority to unload, so plugins can still perform permission checks on this event
    @Subscribe(order = PostOrder.LAST)
    public void onPlayerQuit(DisconnectEvent event) {
        this.minePermsManager.getStorage().unCacheUser(event.getPlayer().getUsername());
    }
}
