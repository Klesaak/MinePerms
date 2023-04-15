package ua.klesaak.mineperms.command;

import ua.klesaak.mineperms.MinePermsManager;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class MinePermsCommand {
    public static final List<String> SUB_COMMANDS_0 = Arrays.asList("user", "group", "reload", "", "", "");
    private final MinePermsManager manager;

    public MinePermsCommand(MinePermsManager manager) {
        this.manager = manager;
    }

    public void invoke(String[] args, Function<String, Object> callback) {

    }
}
