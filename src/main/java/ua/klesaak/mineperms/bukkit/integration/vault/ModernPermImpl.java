package ua.klesaak.mineperms.bukkit.integration.vault;

import org.bukkit.OfflinePlayer;
import ua.klesaak.mineperms.bukkit.MinePermsBukkit;
import ua.klesaak.mineperms.manager.storage.entity.Group;

public abstract class ModernPermImpl extends BasicPermImpl {

	public ModernPermImpl(MinePermsBukkit plugin) {
		super(plugin);
	}

	@Override
	public boolean groupHas(String worldName, String groupName, String permission) {
		Group group = this.storage.getGroup(groupName);
		if (group != null) {
			return group.hasPermission(permission);
		}
		return false;
	}

	@Override
	public boolean playerInGroup(String world, OfflinePlayer player, String groupName) {
		return this.storage.hasPlayerInGroup(player.getName(), groupName);
	}

	@Override
	public String[] getPlayerGroups(String world, OfflinePlayer player) {
		return this.storage.getUserInheritedGroups(player.getName()).toArray(new String[0]);
	}

	@Override
	public String getPrimaryGroup(String world, OfflinePlayer player) {
		return this.storage.getUserGroup(player.getName());
	}

	@Override
	public boolean playerHas(String world, OfflinePlayer player, String permission) {
		return this.storage.hasPermission(player.getName(), permission);
	}

	@Override
	public boolean groupAdd(String worldName, String groupName, String permission) {
		Group group = this.storage.getGroup(groupName);
		if (group != null) {
			this.storage.addGroupPermission(groupName.toLowerCase(), permission);
			return true;
		}
		return false;
	}

	@Override
	public boolean groupRemove(String worldName, String groupName, String permission) {
		Group group = this.storage.getGroup(groupName);
		if (group != null) {
			this.storage.removeGroupPermission(groupName.toLowerCase(), permission);
			return true;
		}
		return false;
	}

	@Override
	public boolean playerAdd(String world, OfflinePlayer player, String permission) {
		this.storage.addUserPermission(player.getName(), permission);
		return true;
	}

	@Override
	public boolean playerRemove(String world, OfflinePlayer player, String permission) {
		this.storage.removeUserPermission(player.getName(), permission);
		return true;
	}

	@Override
	public boolean playerAddGroup(String world, OfflinePlayer player, String groupName) {
		this.storage.setUserGroup(player.getName(), groupName.toLowerCase());
		return true;
	}

	@Override
	public boolean playerRemoveGroup(String world, OfflinePlayer player, String groupName) {
		this.storage.setUserGroup(player.getName(), this.storage.getDefaultGroup().getGroupId());
		return true;
	}

}