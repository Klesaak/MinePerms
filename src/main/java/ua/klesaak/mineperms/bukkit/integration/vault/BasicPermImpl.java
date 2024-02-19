package ua.klesaak.mineperms.bukkit.integration.vault;

import net.milkbowl.vault.permission.Permission;
import ua.klesaak.mineperms.bukkit.MinePermsBukkit;
import ua.klesaak.mineperms.manager.storage.Storage;

public abstract class BasicPermImpl extends Permission {
	protected final MinePermsBukkit plugin;
	protected final Storage storage;

	protected BasicPermImpl(MinePermsBukkit plugin) {
		this.plugin = plugin;
		this.storage = plugin.getMinePerms().getStorage();
	}

	@Override
	public String getName() {
		return plugin.getName();
	}

	@Override
	public boolean hasGroupSupport() {
		return true;
	}

	@Override
	public boolean hasSuperPermsCompat() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String[] getGroups() {
		return this.storage.getGroupNames().toArray(new String[0]);
	}

}