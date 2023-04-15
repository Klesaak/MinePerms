package ua.klesaak.mineperms;

import lombok.Getter;
import org.bukkit.entity.Player;
import ua.klesaak.mineperms.command.MinePermsCommand;

@Getter
public final class MinePermsManager {
    private final MinePermsCommand minePermsCommand;

    public MinePermsManager() {
        this.minePermsCommand = new MinePermsCommand(this);
    }

    public boolean hasPermission(Player player, String permission) {
        //todo логики звезды, антиправа итп.
        return false;
    }

}
