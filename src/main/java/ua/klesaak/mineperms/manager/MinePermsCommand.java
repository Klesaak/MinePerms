package ua.klesaak.mineperms.manager;

import com.google.common.base.Joiner;
import ua.klesaak.mineperms.MinePermsManager;

import java.util.Arrays;
import java.util.List;

public class MinePermsCommand {
    public static final List<String> SUB_COMMANDS_0 = Arrays.asList("user", "group", "reload", "bulkupdate", "find", "export");
    private final MinePermsManager manager;

    public MinePermsCommand(MinePermsManager manager) {
        this.manager = manager;
    }

    public String invoke(String[] args) {
        if (args.length == 0) {
            return this.listMessages(
                    "§6MinePerms by Klesaak §cv1.0",
                    "",
                    "§6/mineperms user <nickname> - user operations command.",
                    "§6/mineperms group <groupID> - group operations command.",
                    "§6/mineperms bulkupdate - bulkupdate operations command.",
                    "§6/mineperms find <permission/groupID> - find all users with perm or group.",
                    "§6/mineperms export <from> <to> - export data from another backend."
            );
        }

        return "";
    }

    private String listMessages(String... messages) {
        return Joiner.on('\n').join(messages);
    }
}
