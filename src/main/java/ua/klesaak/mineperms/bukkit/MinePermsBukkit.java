package ua.klesaak.mineperms.bukkit;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.dependency.Dependency;
import org.bukkit.plugin.java.annotation.dependency.LoadBefore;
import org.bukkit.plugin.java.annotation.dependency.SoftDependency;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.permission.Permissions;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.Website;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import ua.klesaak.mineperms.MinePermsManager;
import ua.klesaak.mineperms.bukkit.integration.PermissibleOverride;
import ua.klesaak.mineperms.bukkit.integration.WorldEditPermissionProvider;
import ua.klesaak.mineperms.bukkit.integration.vault.VaultIntegration;
import ua.klesaak.mineperms.manager.MinePermsCommand;

import java.util.logging.Level;

@Plugin(name = "MinePermsBukkit", version = "1.0")
@Author("Klesaak")
@Dependency("Vault")
@SoftDependency("WorldEdit")
@LoadBefore("WorldEdit")
@Website("https://t.me/klesaak")
@Commands({
        @Command(name = "mineperms", aliases = {"mp", "mperms", "perms"}, desc = "Admin command.", permission = MinePermsCommand.MAIN_PERMISSION)
})
@Description("Simple high performance permission plugin.")
@Permissions({
        @Permission(name = MinePermsCommand.MAIN_PERMISSION, defaultValue = PermissionDefault.OP, desc = "Access to use admin command.")
})
@Getter
public class MinePermsBukkit extends JavaPlugin {
    private volatile MinePermsManager minePermsManager;
    private VaultIntegration vaultIntegration;

    @Override
    public void onEnable() {
        long time = System.currentTimeMillis();
        this.minePermsManager = new MinePermsManager();
        this.minePermsManager.loadConfig(this.getDataFolder());
        this.minePermsManager.initStorage();
        this.getServer().getOperators().forEach(offlinePlayer -> offlinePlayer.setOp(false));
        //регистрируем классы-интеграции
        if (Bukkit.getPluginManager().getPlugin("WorldEdit") != null) {
            WorldEditPermissionProvider.overrideWEPIF(this);
        }
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            this.vaultIntegration = new VaultIntegration(this);
        }
        //Производим иньекцию онлайн игрокам, заменяя дефолтный оператор прав на оператор нашего плагина. //todo так же если есть игроки онлайн - загрузить их в кеш из бд
        this.getServer().getOnlinePlayers().forEach(player -> PermissibleOverride.injectPlayer(player, new PermissibleOverride(this.minePermsManager, player)));
        new MPBukkitListener(this);
        new MPBukkitCommand(this);
        this.getLogger().log(Level.INFO, "Plugin successfully loaded (" + (System.currentTimeMillis() - time) + "ms) ");
    }


    @Override
    public void onDisable() {
        this.minePermsManager.getStorage().close();
        PermissibleOverride.unInjectPlayers(); //возвращаем дефолтный оператор прав игрокам, дабы избежать NullPointer и сервер продолжил функционировать.
        this.vaultIntegration.unload();
    }
}
