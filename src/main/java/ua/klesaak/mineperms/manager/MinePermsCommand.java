package ua.klesaak.mineperms.manager;

import ua.klesaak.mineperms.MinePermsManager;

import java.util.Arrays;
import java.util.List;

public class MinePermsCommand {
    public static final List<String> SUB_COMMANDS_0 = Arrays.asList("user", "group", "reload", "", "", "");
    private final MinePermsManager manager;

    public MinePermsCommand(MinePermsManager manager) {
        this.manager = manager;
    }

    public String invoke(String[] args) {

        return "";
    }
}
