package ua.klesaak.mineperms.bukkit.integration.vault;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import ua.klesaak.mineperms.bukkit.MinePermsBukkit;

public abstract class ModernChatImpl extends UnsupportedNodesChatImpl {

	public ModernChatImpl(MinePermsBukkit plugin, Permission perms) {
		super(plugin, perms);
	}

	@Override
	public String getPlayerPrefix(String worldName, OfflinePlayer player) {
		return users.getUser(player.getUniqueId()).getEffectivePrefix();
	}

	@Override
	public String getPlayerSuffix(String worldName, OfflinePlayer player) {
		return users.getUser(player.getUniqueId()).getEffectiveSuffix();
	}

	@Override
	public String getGroupPrefix(String world, String groupName) {
		Group group = groups.getGroup(groupName);
		return group != null ? group.getPrefix() : null;
	}

	@Override
	public String getGroupSuffix(String world, String groupName) {
		Group group = groups.getGroup(groupName);
		return group != null ? group.getSuffix() : null;
	}

	@Override
	public void setPlayerPrefix(String worldName, OfflinePlayer player, String prefix) {
		users.getUser(player.getUniqueId()).setPrefix(prefix);
	}

	@Override
	public void setPlayerSuffix(String worldName, OfflinePlayer player, String suffix) {
		users.getUser(player.getUniqueId()).setSuffix(suffix);
	}

	@Override
	public void setGroupPrefix(String world, String groupName, String prefix) {
		Group group = groups.getGroup(groupName);
		if (group != null) {
			group.setPrefix(prefix);
		}
	}

	@Override
	public void setGroupSuffix(String world, String groupName, String suffix) {
		Group group = groups.getGroup(groupName);
		if (group != null) {
			group.setPrefix(suffix);
		}
	}

}