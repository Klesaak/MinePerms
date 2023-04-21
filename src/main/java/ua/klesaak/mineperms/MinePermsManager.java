package ua.klesaak.mineperms;

import lombok.Getter;

import java.util.UUID;

@Getter
public final class MinePermsManager {
    public static final String MAIN_PERMISSION = "mineperms.admin";
    private volatile MinePermsCommand minePermsCommand;

    public MinePermsManager() {
        this.minePermsCommand = new MinePermsCommand(this);
    }

    public boolean hasPermission(UUID playerUUID, String permission) {
        //todo логики звезды, антиправа итп.
        return false;
    }

    //todo метод на dump из одной базы в другую

}
