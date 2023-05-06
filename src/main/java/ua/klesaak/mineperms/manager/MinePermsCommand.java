package ua.klesaak.mineperms.manager;

import com.google.common.base.Joiner;
import ua.klesaak.mineperms.MinePermsManager;

import java.util.Arrays;
import java.util.List;

public class MinePermsCommand {
    public static final String MAIN_PERMISSION = "mineperms.admin";

    public static final List<String> SUB_COMMANDS_0 = Arrays.asList("user", "group", "reload", "bulkupdate", "find", "export");
    public static final List<String> USER_SUB_COMMANDS_0 = Arrays.asList("addperm", "removeperm", "delete", "prefix",
            "suffix", "setoption", "removeoption", "getoption");
    public static final List<String> GROUP_SUB_COMMANDS_0 = Arrays.asList("addperm", "removeperm", "delete", "prefix",
            "suffix", "setoption", "removeoption", "getoption");
    private final MinePermsManager manager;

    public MinePermsCommand(MinePermsManager manager) {
        this.manager = manager;
    }

    public String invoke(String[] args) {
        if (args.length == 0) {
            return this.listMessages(
                    "",
                    "§6MinePerms by Klesaak §cv1.0",
                    "",
                    "§6/mineperms user <nickname> - user operations command.",
                    "§6/mineperms group <groupID> - group operations command.",
                    "§6/mineperms bulkupdate - bulkupdate operations command.",
                    "§6/mineperms find <permission/groupID> - find all users with a specify perm or group.",
                    "§6/mineperms export <from> <to> - export data from another backend."
            );
        }
        //String nickName = args[1];
        switch (args[0].toLowerCase()) {
            case "user": {
                if (args.length == 1) {
                    return this.listMessages(
                            "",
                            "§6/mineperms user addperm <nickname> <permission> - add a specify permission.",
                            "§6/mineperms user removeperm <nickname> <permission> - remove specify permission.",
                            "§6/mineperms user delete <nickname> - delete specify user.",
                            "§6/mineperms user prefix <nickname> <prefix> - set user prefix.",
                            "§6/mineperms user suffix <nickname> <suffix> - set user suffix.",
                            "§6/mineperms user setoption <nickname> <option> - set specify user option.",
                            "§6/mineperms user removeoption <nickname> <option> - remove specify user option.",
                            "§6/mineperms user getoption <nickname> <option> - get specify user option."
                    );
                }
                switch (args[1].toLowerCase()) {
                    case "addperm": {
                        break;
                    }
                    case "removeperm": {
                        break;
                    }
                    case "delete": {
                        break;
                    }
                    case "prefix": {
                        break;
                    }
                    case "suffix": {
                        break;
                    }
                    case "setoption": {
                        break;
                    }
                    case "removeoption": {
                        break;
                    }
                    case "getoption": {
                        break;
                    }
                }
                break;
            }
            case "group": {
                if (args.length == 1) {
                    return this.listMessages(
                            "",
                            "§6/mineperms group addperm <groupID> <permission> - add a specify permission.",
                            "§6/mineperms group removeperm <groupID> <permission> - remove specify permission.",
                            "§6/mineperms group delete <groupID> - delete specify group.",
                            "§6/mineperms group prefix <groupID> <prefix> - set group prefix.",
                            "§6/mineperms group suffix <groupID> <suffix> - set group suffix.",
                            "§6/mineperms group setoption <groupID> <option> - set specify group option.",
                            "§6/mineperms group removeoption <groupID> <option> - remove specify group option.",
                            "§6/mineperms group getoption <groupID> <option> - get specify group option."
                    );
                }
                switch (args[1].toLowerCase()) {
                    case "addperm": {
                        break;
                    }
                    case "removeperm": {
                        break;
                    }
                    case "delete": {
                        break;
                    }
                    case "prefix": {
                        break;
                    }
                    case "suffix": {
                        break;
                    }
                    case "setoption": {
                        break;
                    }
                    case "removeoption": {
                        break;
                    }
                    case "getoption": {
                        break;
                    }
                }
                break;
            }
            case "reload": {
                break;
            }
            case "bulkupdate": {
                break;
            }
            case "find": {
                break;
            }
            case "export": {
                break;
            }
        }

        return "";
    }

    private String listMessages(String... messages) {
        return Joiner.on('\n').join(messages);
    }
}
