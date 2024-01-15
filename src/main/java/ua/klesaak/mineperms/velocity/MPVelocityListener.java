package ua.klesaak.mineperms.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.permission.PermissionsSetupEvent;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import ua.klesaak.mineperms.manager.storage.Storage;

public final class MPVelocityListener {
    private final Storage storage;
    private final MinePermsVelocity plugin;

    public MPVelocityListener(MinePermsVelocity plugin) {
        this.storage = plugin.getMinePermsManager().getStorage();
        this.plugin = plugin;
        plugin.getServer().getEventManager().register(plugin, this);
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPermSetup(PermissionsSetupEvent e) {
        e.setProvider(subject -> permission -> {
            if (!(subject instanceof CommandSource)) return Tristate.FALSE;
            if (subject == this.plugin.getServer().getConsoleCommandSource()) {
                return Tristate.TRUE;
            }
            CommandSource source = (CommandSource) subject;
            String userName = ((Player)source).getUsername();
            boolean res = this.storage.hasPermission(userName, permission);
            return Tristate.fromBoolean(res);
        });
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerLogin(LoginEvent event) {
       // Player player = event.getPlayer();
    }

    @Subscribe
    public void onPlayerPostLogin(LoginEvent event) {
        if (!event.getResult().isAllowed()) return;
        final Player player = event.getPlayer();
        this.storage.cacheUser(player.getUsername());
    }

    // Wait until the last priority to unload, so plugins can still perform permission checks on this event
    @Subscribe(order = PostOrder.LAST)
    public void onPlayerQuit(DisconnectEvent event) {
        this.storage.unCacheUser(event.getPlayer().getUsername());
    }
}
