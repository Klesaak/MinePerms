package ua.klesaak.mineperms.bukkit.integration.vault;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import ua.klesaak.mineperms.bukkit.MinePermsBukkit;
import ua.klesaak.mineperms.manager.storage.Group;

public abstract class ModernChatImpl extends UnsupportedNodesChatImpl {

	public ModernChatImpl(MinePermsBukkit plugin, Permission perms) {
		super(plugin, perms);
	}

	@Override
	public String getPlayerPrefix(String worldName, OfflinePlayer player) {
		return this.storage.getUserPrefix(player.getName());
	}

	@Override
	public String getPlayerSuffix(String worldName, OfflinePlayer player) {
		return this.storage.getUserSuffix(player.getName());
	}

	@Override
	public String getGroupPrefix(String world, String groupName) {
		Group group = this.storage.getGroup(groupName);
		return group != null ? group.getPrefix() : null;
	}

	@Override
	public String getGroupSuffix(String world, String groupName) {
		Group group = this.storage.getGroup(groupName);
		return group != null ? group.getSuffix() : null;
	}

	@Override
	public void setPlayerPrefix(String worldName, OfflinePlayer player, String prefix) {
		this.storage.setUserPrefix(player.getName(), prefix);
	}

	@Override
	public void setPlayerSuffix(String worldName, OfflinePlayer player, String suffix) {
		this.storage.setUserSuffix(player.getName(), suffix);
	}

	@Override
	public void setGroupPrefix(String world, String groupName, String prefix) {
		Group group = this.storage.getGroup(groupName);
		if (group != null) {
			this.storage.setGroupPrefix(groupName, prefix);
		}
	}

	@Override
	public void setGroupSuffix(String world, String groupName, String suffix) {
		Group group = this.storage.getGroup(groupName);
		if (group != null) {
			this.storage.setGroupSuffix(groupName, suffix);
		}
	}

}