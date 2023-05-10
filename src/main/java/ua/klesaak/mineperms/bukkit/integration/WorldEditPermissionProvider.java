package ua.klesaak.mineperms.bukkit.integration;

import com.sk89q.wepif.PermissionsProvider;
import com.sk89q.wepif.PermissionsResolver;
import com.sk89q.wepif.PermissionsResolverManager;
import lombok.SneakyThrows;
import org.bukkit.OfflinePlayer;
import ua.klesaak.mineperms.bukkit.MinePermsBukkit;
import ua.klesaak.mineperms.manager.storage.Storage;

import java.lang.reflect.Field;
import java.util.logging.Level;

public class WorldEditPermissionProvider implements PermissionsProvider, PermissionsResolver {
    private final Storage storage;

    public WorldEditPermissionProvider(MinePermsBukkit plugin) {
        this.storage = plugin.getMinePermsManager().getStorage();
        plugin.getLogger().log(Level.INFO, "WEPIF successfully overriding.");
    }

    @SneakyThrows
    public static void overrideWEPIF(MinePermsBukkit plugin) {
        PermissionsResolverManager permissionsResolverManager = PermissionsResolverManager.getInstance();
        Field permissionResolverField = permissionsResolverManager.getClass().getDeclaredField("permissionResolver");
        permissionResolverField.setAccessible(true);
        permissionResolverField.set(permissionsResolverManager, new WorldEditPermissionProvider(plugin));
    }

    @Override
    public boolean hasPermission(String name, String permission) {
        return this.storage.hasPermission(name, permission);
    }

    @Override
    public boolean hasPermission(String worldName, String name, String permission) {
        return this.hasPermission(name, permission);
    }

    @Override
    public boolean inGroup(String player, String group) {
        return this.storage.playerInGroup(player, group);
    }

    @Override
    public String[] getGroups(String player) {
        return this.storage.getUserInheritedGroups(player).toArray(new String[0]);
    }

    @Override
    public boolean hasPermission(OfflinePlayer player, String permission) {
        return this.hasPermission(player.getName(), permission);
    }

    @Override
    public boolean hasPermission(String worldName, OfflinePlayer player, String permission) {
        return this.hasPermission(player.getName(), permission);
    }

    @Override
    public boolean inGroup(OfflinePlayer player, String group) {
        return this.inGroup(player.getName(), group);
    }

    @Override
    public String[] getGroups(OfflinePlayer player) {
        return this.getGroups(player.getName());
    }

    @Override
    public void load() {}

    @Override
    public String getDetectionMessage() {
        return "MinePerms is detected.";
    }
}
