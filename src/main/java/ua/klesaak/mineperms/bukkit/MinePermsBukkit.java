package ua.klesaak.mineperms.bukkit;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.dependency.Dependency;
import org.bukkit.plugin.java.annotation.dependency.LoadBefore;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.permission.Permissions;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.Website;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import ua.klesaak.mineperms.MinePermsManager;
import ua.klesaak.mineperms.bukkit.integration.PermissibleOverride;

import java.util.logging.Level;


@Plugin(name = "MinePermsBukkit", version = "1.0")
@Author("Klesaak")
@Dependency("Vault")
@LoadBefore("Vault")
@Website("https://t.me/klesaak")
@Commands({@Command(name = "mineperms", aliases = {"mp", "mperms", "perms"}, desc = "Admin command.", permission = "mineperms.admin")})

@Description("Simple high performance permission plugin.")
@Permissions({
        @Permission(name = "mineperms.admin", defaultValue = PermissionDefault.OP, desc = "Access to use admin command.")
})
@Getter
public class MinePermsBukkit extends JavaPlugin {
    @Getter private static MinePermsManager minePermsManager;

    @Override
    public void onEnable() {
        long time = System.currentTimeMillis();
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            this.getLogger().log(Level.SEVERE, "Vault is not found! Disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        minePermsManager = new MinePermsManager();
        this.getServer().getOperators().forEach(offlinePlayer -> offlinePlayer.setOp(false));
        this.getServer().getOnlinePlayers().forEach(player -> PermissibleOverride.injectPlayer(player, new PermissibleOverride(player)));
        new MinePermsListener(this);
        new MPBukkitCommand(this);
        this.getLogger().log(Level.INFO, "Plugin successfully loaded (" + (System.currentTimeMillis() - time) + "ms) ");
    }


    @Override
    public void onDisable() {
        //todo unInject PermissibleOverride
    }
}
