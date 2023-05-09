package ua.klesaak.mineperms.bukkit.integration.vault;

import net.milkbowl.vault.permission.Permission;
import ua.klesaak.mineperms.bukkit.MinePermsBukkit;

public abstract class UnsupportedNodesChatImpl extends BaseChatImpl {

	public UnsupportedNodesChatImpl(MinePermsBukkit plugin, Permission perms) {
		super(plugin, perms);
	}

	@Override
	public int getPlayerInfoInteger(String p0, String p1, String p2, int p3) {
		throw new UnsupportedOperationException("Info nodes are not supported");
	}

	@Override
	public void setPlayerInfoInteger(String p0, String p1, String p2, int p3) {
		throw new UnsupportedOperationException("Info nodes are not supported");
	}


	@Override
	public double getPlayerInfoDouble(String p0, String p1, String p2, double p3) {
		throw new UnsupportedOperationException("Info nodes are not supported");
	}

	@Override
	public void setPlayerInfoDouble(String p0, String p1, String p2, double p3) {
		throw new UnsupportedOperationException("Info nodes are not supported");
	}

	@Override
	public double getGroupInfoDouble(String p0, String p1, String p2, double p3) {
		throw new UnsupportedOperationException("Info nodes are not supported");
	}

	@Override
	public boolean getPlayerInfoBoolean(String p0, String p1, String p2, boolean p3) {
		throw new UnsupportedOperationException("Info nodes are not supported");
	}

	@Override
	public void setPlayerInfoBoolean(String p0, String p1, String p2, boolean p3) {
		throw new UnsupportedOperationException("Info nodes are not supported");
	}

	@Override
	public String getPlayerInfoString(String p0, String p1, String p2, String p3) {
		throw new UnsupportedOperationException("Info nodes are not supported");
	}

	@Override
	public int getGroupInfoInteger(String p0, String p1, String p2, int p3) {
		throw new UnsupportedOperationException("Info nodes are not supported");
	}

	@Override
	public void setGroupInfoInteger(String p0, String p1, String p2, int p3) {
		throw new UnsupportedOperationException("Info nodes are not supported");
	}

	@Override
	public void setPlayerInfoString(String p0, String p1, String p2, String p3) {
		throw new UnsupportedOperationException("Info nodes are not supported");
	}

	@Override
	public void setGroupInfoDouble(String p0, String p1, String p2, double p3) {
		throw new UnsupportedOperationException("Info nodes are not supported");
	}

	@Override
	public boolean getGroupInfoBoolean(String p0, String p1, String p2, boolean p3) {
		throw new UnsupportedOperationException("Info nodes are not supported");
	}

	@Override
	public void setGroupInfoBoolean(String p0, String p1, String p2, boolean p3) {
		throw new UnsupportedOperationException("Info nodes are not supported");
	}

	@Override
	public String getGroupInfoString(String worldName, String groupName, String node, String value) {
		throw new UnsupportedOperationException("Info nodes are not supported");
	}

	@Override
	public void setGroupInfoString(String worldName, String groupName, String node, String value) {
		throw new UnsupportedOperationException("Info nodes are not supported");
	}

}