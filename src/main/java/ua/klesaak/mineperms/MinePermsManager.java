package ua.klesaak.mineperms;

import lombok.Getter;
import ua.klesaak.mineperms.manager.MinePermsCommand;

import java.util.UUID;

@Getter
public final class MinePermsManager {
    public static final String MAIN_PERMISSION = "mineperms.admin";

    public static final String WILDCARD_SUFFIX = ".*";
    public static final String ROOT_WILDCARD = "*";
    public static final String ROOT_WILDCARD_WITH_QUOTES = "'*'";
    private volatile MinePermsCommand minePermsCommand;

    public MinePermsManager() {
        this.minePermsCommand = new MinePermsCommand(this);
    }

    public boolean hasPermission(UUID playerUUID, String permission) {
        return false;
    }

    //todo метод на dump из одной базы в другую

}
