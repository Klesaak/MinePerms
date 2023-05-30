package ua.klesaak.mineperms.velocity.listeners;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.permission.PermissionsSetupEvent;
import com.velocitypowered.api.proxy.Player;
import ua.klesaak.mineperms.MinePermsManager;
import ua.klesaak.mineperms.velocity.MinePermsVelocity;

public class MPVelocityListener {
   /* private final MinePermsManager minePermsManager;

    public MPVelocityListener(MinePermsVelocity plugin) {
        this.minePermsManager = plugin.getMinePermsManager();
        plugin.getServer().getEventManager().register(plugin, this);
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerLogin(LoginEvent e) {
        final Player player = e.getPlayer();
        if (this.deniedLogin.remove(player.getUniqueId())) {
            e.setResult(AdventureCompat.deniedResult(TranslationManager.render(Message.LOADING_DATABASE_ERROR.build(), player.getPlayerSettings().getLocale())));
        }
    }

    @Subscribe
    public void onPlayerPostLogin(LoginEvent e) {
        final Player player = e.getPlayer();
        final User user = this.plugin.getUserManager().getIfLoaded(e.getPlayer().getUniqueId());

        if (this.plugin.getConfiguration().get(ConfigKeys.DEBUG_LOGINS)) {
            this.plugin.getLogger().info("Processing post-login for " + player.getUniqueId() + " - " + player.getUsername());
        }

        if (!e.getResult().isAllowed()) {
            return;
        }

        if (user == null) {
            if (!getUniqueConnections().contains(player.getUniqueId())) {
                this.plugin.getLogger().warn("User " + player.getUniqueId() + " - " + player.getUsername() +
                        " doesn't have data pre-loaded, they have never been processed during pre-login in this session.");
            } else {
                this.plugin.getLogger().warn("User " + player.getUniqueId() + " - " + player.getUsername() +
                        " doesn't currently have data pre-loaded, but they have been processed before in this session.");
            }

            if (this.plugin.getConfiguration().get(ConfigKeys.CANCEL_FAILED_LOGINS)) {
                // disconnect the user
                e.setResult(AdventureCompat.deniedResult(TranslationManager.render(Message.LOADING_STATE_ERROR.build(), player.getPlayerSettings().getLocale())));
            } else {
                // just send a message
                this.plugin.getBootstrap().getScheduler().asyncLater(() -> {
                    if (!player.isActive()) {
                        return;
                    }

                    Message.LOADING_STATE_ERROR.send(this.plugin.getSenderFactory().wrap(player));
                }, 1, TimeUnit.SECONDS);
            }
        }
    }

    // Wait until the last priority to unload, so plugins can still perform permission checks on this event
    @Subscribe(order = PostOrder.LAST)
    public void onPlayerQuit(DisconnectEvent e) {
        handleDisconnect(e.getPlayer().getUniqueId());
    }*/
}
