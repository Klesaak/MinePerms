package ua.klesaak.mineperms.bukkit.integration.vault;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import ua.klesaak.mineperms.bukkit.MinePermsBukkit;
import ua.klesaak.mineperms.manager.storage.Storage;

public abstract class BaseChatImpl extends Chat {
	protected final MinePermsBukkit plugin;
	protected final Storage storage;

	protected BaseChatImpl(MinePermsBukkit plugin, Permission perms) {
		super(perms);
		this.plugin = plugin;
		this.storage = plugin.getMinePerms().getStorage();
	}

	@Override
	public String getName() {
		return plugin.getName();
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}