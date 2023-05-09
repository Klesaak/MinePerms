package ua.klesaak.mineperms.bukkit.integration.vault;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import ua.klesaak.mineperms.bukkit.MinePermsBukkit;

import java.util.Objects;

public class VaultIntegration {
    protected final MinePermsBukkit plugin;
    private final Permission permissionImpl;
    private final Chat chatImpl;

    public VaultIntegration(MinePermsBukkit plugin) {
        this.plugin = plugin;
        this.permissionImpl = new DeprecatedPermImpl(plugin);
        this.chatImpl = new DeprecatedChatImpl(plugin, this.permissionImpl);
        plugin.getServer().getServicesManager().register(Permission.class, this.permissionImpl, plugin, ServicePriority.High);
        plugin.getServer().getServicesManager().register(Chat.class, this.chatImpl, plugin, ServicePriority.High);
        plugin.getLogger().info("Overriding Vault Permission and Chat service...");
        plugin.getLogger().info("Chat: " + (Objects.requireNonNull(Bukkit.getServicesManager().getRegistration(Chat.class)).getProvider()).getName());
        plugin.getLogger().info("Permission: " + (Objects.requireNonNull(Bukkit.getServicesManager().getRegistration(Permission.class)).getProvider()).getName());
    }

    public void unload() {
        this.plugin.getServer().getServicesManager().unregister(this.permissionImpl);
        this.plugin.getServer().getServicesManager().unregister(this.chatImpl);
    }
}