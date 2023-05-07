package ua.klesaak.mineperms.bukkit.integration;

import com.sk89q.wepif.PermissionsProvider;
import org.bukkit.OfflinePlayer;
import ua.klesaak.mineperms.bukkit.MinePermsBukkit;

public class WorldEditPermissionProvider implements PermissionsProvider {
    private final MinePermsBukkit plugin;

    public WorldEditPermissionProvider(MinePermsBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean hasPermission(String name, String permission) {
        return this.plugin.getMinePermsManager().hasPermission(name, permission);
    }

    @Override
    public boolean hasPermission(String worldName, String name, String permission) {
        return this.hasPermission(name, permission);
    }

    @Override
    public boolean inGroup(String player, String group) {
        return false;
    }

    @Override
    public String[] getGroups(String player) {
        return new String[0];
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
        return false;
    }

    @Override
    public String[] getGroups(OfflinePlayer player) {
        return new String[0];
    }
}
