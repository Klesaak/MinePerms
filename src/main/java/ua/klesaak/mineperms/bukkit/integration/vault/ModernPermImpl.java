package ua.klesaak.mineperms.bukkit.integration.vault;

import org.bukkit.OfflinePlayer;
import ua.klesaak.mineperms.bukkit.MinePermsBukkit;
import ua.klesaak.mineperms.manager.storage.Group;
import ua.klesaak.mineperms.manager.storage.User;

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
		Group group = this.storage.getGroup(groupName);
		if (group == null) {
			return false;
		}
		return this.storage.getUser(player.getName()).getGroup().equalsIgnoreCase(groupName);
	}

	@Override
	public String[] getPlayerGroups(String world, OfflinePlayer player) {
		return this.storage.getUserInheritedGroups(player.getName()).toArray(new String[0]);
	}

	@Override
	public String getPrimaryGroup(String world, OfflinePlayer player) {
		User user = this.storage.getUser(player.getName());
		return user.getGroup();
	}

	@Override
	public boolean playerHas(String world, OfflinePlayer player, String permission) {
		User user = this.storage.getUser(player.getName());
		return user.hasPermission(permission);
	}

	@Override
	public boolean groupAdd(String worldName, String groupName, String permission) {
		Group group = this.storage.getGroup(groupName);
		if (group != null) {
			group.addPermission(permission);
			this.storage.recalculateUsersPermissions();
			return true;
		}
		return false;
	}

	@Override
	public boolean groupRemove(String worldName, String groupName, String permission) {
		Group group = this.storage.getGroup(groupName);
		if (group != null) {
			group.removePermission(permission);
			this.storage.recalculateUsersPermissions();
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
		User user = users.getUser(player.getUniqueId());
		if (user.hasAdditionalPermission(permission)) {
			user.removeAdditionalPermission(permission);
		} else {
			user.addAdditionalPermission("-"+permission);
		}
		bperms.updatePermissions(player);
		return true;
	}

	@Override
	public boolean playerAddGroup(String world, OfflinePlayer player, String groupName) {
		Group group = groups.getGroup(groupName);
		if (group == null) {
			return false;
		}
		User user = users.getUser(player.getUniqueId());
		if (user.getMainGroup() == groups.getDefaultGroup()) {
			user.setMainGroup(group);
		} else {
			user.addSubGroup(group);
		}
		bperms.updatePermissions(player);
		return true;
	}

	@Override
	public boolean playerRemoveGroup(String world, OfflinePlayer player, String groupName) {
		Group group = groups.getGroup(groupName);
		if (group == null) {
			return false;
		}
		User user = users.getUser(player.getUniqueId());
		user.removeSubGroup(group);
		if (user.getMainGroup() == group) {
			user.setMainGroup(groups.getDefaultGroup());
		}
		bperms.updatePermissions(player);
		return true;
	}

}