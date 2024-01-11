package ua.klesaak.mineperms.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import org.slf4j.Logger;
import ua.klesaak.mineperms.MinePermsManager;
import ua.klesaak.mineperms.manager.event.velocity.VelocityEventManager;
import ua.klesaak.mineperms.manager.log.MPLogger;
import ua.klesaak.mineperms.manager.utils.Platform;

import java.nio.file.Path;

@Plugin(id = "minepermsvelocity",
        name = "MinePermsVelocity",
        version = "1.0-SNAPSHOT",
        url = "https://t.me/klesaak",
        description = "Simple high performance permission plugin.",
        authors = {"Klesaak"}
)
@Getter
public final class MinePermsVelocity {
    private final ProxyServer server;
    private final Logger logger;
    private final MinePermsManager minePermsManager;

    @Inject
    public MinePermsVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        long time = System.currentTimeMillis();
        this.server = server;
        this.logger = logger;
        this.minePermsManager = new MinePermsManager(Platform.VELOCITY);
        this.minePermsManager.init(dataDirectory.toFile(), new VelocityEventManager(this));
        MPLogger.register(this.logger);
        this.logger.info("Plugin successfully loaded (" + (System.currentTimeMillis() - time) + "ms) ");
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        new MPVelocityCommand(this);
        new MPVelocityListener(this);
    }
}
