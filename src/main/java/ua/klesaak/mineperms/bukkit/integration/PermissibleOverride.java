package ua.klesaak.mineperms.bukkit.integration;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import ua.klesaak.mineperms.bukkit.utils.BukkitUtils;
import ua.klesaak.mineperms.manager.storage.Storage;
import ua.klesaak.mineperms.manager.utils.PermissionsMatcher;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class PermissibleOverride extends PermissibleBase {
    private static final Field HUMAN_ENTITY_PERMISSIBLE_FIELD;
    static {
        try {
            // Try to load the permissible field.
            Field humanEntityPermissibleField;
            try {
                // craftbukkit
                humanEntityPermissibleField = BukkitUtils.obcClass("entity.CraftHumanEntity").getDeclaredField("perm");
                humanEntityPermissibleField.setAccessible(true);
            } catch (Exception e) {
                // glowstone
                humanEntityPermissibleField = Class.forName("net.glowstone.entity.GlowHumanEntity").getDeclaredField("permissions");
                humanEntityPermissibleField.setAccessible(true);
            }
            HUMAN_ENTITY_PERMISSIBLE_FIELD = humanEntityPermissibleField;
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @SneakyThrows
    public static void injectPlayer(Player player, PermissibleOverride permissibleOverride) {
        PermissibleBase oldPermissible = (PermissibleBase) HUMAN_ENTITY_PERMISSIBLE_FIELD.get(player);
        if (oldPermissible instanceof PermissibleOverride) throw new IllegalStateException("PermissibleOverride already injected into player " + player.getName());
        oldPermissible.clearPermissions();

        // inject the new instance
        HUMAN_ENTITY_PERMISSIBLE_FIELD.set(player, permissibleOverride);
    }

    @SneakyThrows
    public static void unInjectPlayers() {
        for (val onlinePlayer : Bukkit.getOnlinePlayers()) {
            HUMAN_ENTITY_PERMISSIBLE_FIELD.set(onlinePlayer, new PermissibleBase(onlinePlayer));
        }
    }

    private final Storage storage;
    private final String playerName;

    public PermissibleOverride(String playerName, Storage storage) {
        super(null);
        this.storage = storage;
        this.playerName = playerName;
    }

    @Override
    public boolean isOp() {
        return this.hasPermission(PermissionsMatcher.ROOT_WILDCARD);
    }

    @Override
    public void setOp(boolean value) {
    }

    @Override
    public boolean isPermissionSet(@NonNull String name) {
        return this.hasPermission(name);
    }

    @Override
    public boolean isPermissionSet(Permission perm) {
        return this.isPermissionSet(perm.getName());
    }

    @Override
    public boolean hasPermission(@NonNull String inName) {
        return this.storage.hasPermission(this.playerName, inName);
    }

    @Override
    public boolean hasPermission(Permission perm) {
        return this.hasPermission(perm.getName());
    }

    @Override @NonNull
    public PermissionAttachment addAttachment(@NonNull Plugin plugin, @NonNull String name, boolean value) {
        return this.addAttachment(plugin);
    }

    @Override @NonNull
    public PermissionAttachment addAttachment(@NonNull Plugin plugin) {
        return Objects.requireNonNull(this.addAttachment(plugin, 0));
    }

    @Override
    public void removeAttachment(@NonNull PermissionAttachment attachment) {
    }

    @Override
    public void recalculatePermissions() {
    }

    @Override
    public synchronized void clearPermissions() {
    }

    @Override
    public PermissionAttachment addAttachment(@NonNull Plugin plugin, @NonNull String name, boolean value, int ticks) {
        return this.addAttachment(plugin, ticks);
    }

    @Override
    public PermissionAttachment addAttachment(@NonNull Plugin plugin, int ticks) {
       return new PermissionAttachment(plugin, this);
    }

    @Override @NonNull
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return Collections.emptySet();
    }
}
