package ua.klesaak.mineperms.manager;

import com.google.common.base.Joiner;
import ua.klesaak.mineperms.MinePermsManager;
import ua.klesaak.mineperms.manager.storage.Group;
import ua.klesaak.mineperms.manager.storage.Storage;
import ua.klesaak.mineperms.manager.storage.User;

import java.util.Arrays;
import java.util.List;

public class MinePermsCommand {
    public static final String MAIN_PERMISSION = "mineperms.admin";

    public static final List<String> SUB_COMMANDS_0 = Arrays.asList("user", "group", "reload", "bulkupdate", "find", "export");
    public static final List<String> USER_SUB_COMMANDS_0 = Arrays.asList("addperm", "removeperm", "info", "setgroup", "delete", "prefix",
            "suffix", "clear-prefix", "clear-suffix", "setoption", "removeoption", "getoption");
    public static final List<String> GROUP_SUB_COMMANDS_0 = Arrays.asList("addperm", "removeperm", "info", "delete", "prefix",
            "suffix", "clear-prefix", "clear-suffix", "setoption", "create", "add-parent", "remove-parent", "removeoption", "getoption");
    private final MinePermsManager manager;

    public MinePermsCommand(MinePermsManager manager) {
        this.manager = manager;
    }

    public String invoke(String label, String[] args) {
        if (args.length == 0) {
            return this.listMessages(
                    "",
                    "§6MinePerms by Klesaak §cv1.0",
                    "",
                    "§6/" + label + " user - user operations command.",
                    "§6/" + label + " group - group operations command.",
                    "§6/" + label + " bulkupdate - bulkupdate operations command.",
                    "§6/" + label + " export <from> <to> - export data from another backend."
            );
        }
        Storage storage = this.manager.getStorage();
        switch (args[0].toLowerCase()) {
            case "user": {
                if (args.length == 1) {
                    return this.listMessages(
                            "",
                            "§6/" + label +" user info <nickname> - show info of a player.",
                            "§6/" + label +" user addperm <nickname> <permission> - add a specify permission.",
                            "§6/" + label +" user removeperm <nickname> <permission> - remove specify permission.",
                            "§6/" + label +" user setgroup <nickname> <groupID> - set a specify group.",
                            "§6/" + label +" user delete <nickname> - delete specify user.",
                            "§6/" + label +" user prefix <nickname> <prefix> - set user prefix.",
                            "§6/" + label +" user suffix <nickname> <suffix> - set user suffix.",
                            "§6/" + label +" user clear-prefix <nickname> - clear user prefix.",
                            "§6/" + label +" user clear-suffix <nickname> - clear user suffix.",
                            "§6/" + label +" user setoption <nickname> <option> - set specify user option.",
                            "§6/" + label +" user removeoption <nickname> <option> - remove specify user option.",
                            "§6/" + label +" user getoption <nickname> <option> - get specify user option."
                    );
                }
                switch (args[1].toLowerCase()) {
                    case "info": {
                        if (args.length != 3) return "§6/" + label +" user info <nickname> - show info of a player.";
                        String nickName = args[2];
                        User user = this.manager.getStorage().getUser(nickName);
                        if (user == null) return "§cUser not found!";
                        String perms = user.getPermissions().isEmpty() ? "§cPermissions not set!" : '\n' + "  " + Joiner.on('\n' + "  ").join(user.getPermissions());
                        return this.listMessages(
                                "",
                                "§aUser " + nickName + " info:",
                                " §aPermissions: " + perms,
                                " §aGroup: §6" + user.getGroup(),
                                " §aPrefix: §6" + (user.getPrefix().isEmpty() ? "§cNot set." : user.getPrefix()),
                                " §aSuffix: §6" + (user.getSuffix().isEmpty() ? "§cNot set." : user.getSuffix()),
                                " §aOptions: TODO");
                    }
                    case "addperm": {
                        if (args.length != 4) return "§6/" + label +" user addperm <nickname> <permission> - add a specify permission.";
                        String nickName = args[2];
                        String permission = args[3];
                        storage.addUserPermission(nickName, permission);
                        return "§6Permission §c" + permission + " §6added to §a" + nickName;
                    }
                    case "removeperm": {
                        if (args.length != 4) return "§6/" + label +" user removeperm <nickname> <permission> - remove a specify permission.";
                        String nickName = args[2];
                        String permission = args[3];
                        storage.removeUserPermission(nickName, permission);
                        return "§6Permission §c" + permission + " §6removed from §a" + nickName;
                    }
                    case "setgroup": {
                        if (args.length != 4) return "§6/" + label +" user setgroup <nickname> <groupID> - set a specify group.";
                        String nickName = args[2];
                        String groupID = args[3].toLowerCase();
                        Group group = storage.getGroup(groupID);
                        if (group == null) return "§cGroup not found!";
                        storage.setUserGroup(nickName, groupID);
                        return "§6Group §c" + groupID + " §6set to §a" + nickName;
                    }
                    case "delete": {
                        if (args.length != 3) return "§6/" + label +" user delete <nickname> - delete specify user.";
                        return "§cTODO delete method";
                    }
                    case "prefix": {
                        if (args.length < 4) return "§6/" + label +" user prefix <nickname> <prefix> - set user prefix.";
                        String nickName = args[2];
                        String prefix = this.getFinalArg(args, 3);
                        storage.setUserPrefix(nickName, prefix);
                        return "§6Prefix §c" + prefix + " §6set to §a" + nickName;
                    }
                    case "suffix": {
                        if (args.length < 4) return "§6/" + label +" user suffix <nickname> <prefix> - set user suffix.";
                        String nickName = args[2];
                        String suffix = this.getFinalArg(args, 3);
                        storage.setUserSuffix(nickName, suffix);
                        return "§6Suffix §c" + suffix + " §6set to §a" + nickName;
                    }
                    case "clear-prefix": {
                        if (args.length != 3) return "§6/" + label +" user clear-prefix <nickname> - clear user prefix.";
                        String nickName = args[2];
                        storage.setUserPrefix(nickName, "");
                        return "§6Prefix remove from §a" + nickName;
                    }
                    case "clear-suffix": {
                        if (args.length != 3) return "§6/" + label +" user clear-suffix <nickname> - clear user suffix.";
                        String nickName = args[2];
                        storage.setUserSuffix(nickName, "");
                        return "§6Suffix remove from §a" + nickName;
                    }
                    case "setoption": {
                        if (args.length != 4) return "§6/" + label +" user setoption <nickname> <option> - set specify group option.";
                        return "§cTODO setoption method";
                    }
                    case "removeoption": {
                        if (args.length != 4) return "§6/" + label +" user removeoption <nickname> <option> - remove specify group option.";
                        return "§cTODO removeoption method";
                    }
                    case "getoption": {
                        if (args.length != 4) return "§6/" + label +" user getoption <nickname> <option> - get specify group option.";

                        return "§cTODO getoption method";
                    }
                }
                break;
            }
            case "group": {
                if (args.length == 1) {
                    return this.listMessages(
                            "",
                            "§6/" + label +" group info <groupID> - show info of a group.",
                            "§6/" + label +" group addperm <groupID> <permission> - add a specify permission.",
                            "§6/" + label +" group removeperm <groupID> <permission> - remove specify permission.",
                            "§6/" + label +" group add-parent <groupID> <parentGroupID> - add a specify parent group.",
                            "§6/" + label +" group remove-parent <groupID> <parentGroupID> - remove a specify parent group.",
                            "§6/" + label +" group delete <groupID> - delete specify group.",
                            "§6/" + label +" group create <groupID> - create specify group.",
                            "§6/" + label +" group prefix <groupID> <prefix> - set group prefix.",
                            "§6/" + label +" group suffix <groupID> <suffix> - set group suffix.",
                            "§6/" + label +" group clear-prefix <groupID> - clear group prefix.",
                            "§6/" + label +" group clear-suffix <groupID> - clear group suffix.",
                            "§6/" + label +" group setoption <groupID> <option> - set specify group option.",
                            "§6/" + label +" group removeoption <groupID> <option> - remove specify group option.",
                            "§6/" + label +" group getoption <groupID> <option> - get specify group option."
                    );
                }
                switch (args[1].toLowerCase()) {
                    case "info": {
                        if (args.length != 3) return "§6/" + label +" group info <groupID> - show info of a group.";
                        String groupID = args[2];
                        Group group = storage.getGroup(groupID);
                        if (group == null) return "§cGroup not found!";
                        String perms = group.getPermissions().isEmpty() ? "§cPermissions not set!" : '\n' + "  " + Joiner.on('\n' + "  " ).join(group.getPermissions());
                        return this.listMessages(
                                "",
                                "§aGroup " + groupID + " info:",
                                " §aPermissions: " + perms,
                                " §aPrefix: §6" + (group.getPrefix().isEmpty() ? "§cNot set." : group.getPrefix()),
                                " §aSuffix: §6" + (group.getSuffix().isEmpty() ? "§cNot set." : group.getSuffix()),
                                " §aOptions: TODO");
                    }
                    case "addperm": {
                        if (args.length != 4) return "§6/" + label +" group addperm <groupID> <permission> - add a specify permission.";
                        String groupID = args[2].toLowerCase();
                        String permission = args[3];
                        Group group = storage.getGroup(groupID);
                        if (group == null) return "§cGroup not found!";
                        storage.addGroupPermission(groupID, permission);
                        return "§6Permission §c" + permission + " §6added to group §a" + groupID;
                    }
                    case "removeperm": {
                        if (args.length != 4) return "§6/" + label +" group removeperm <groupID> <permission> - remove a specify permission.";
                        String groupID = args[2].toLowerCase();
                        String permission = args[3];
                        Group group = storage.getGroup(groupID);
                        if (group == null) return "§cGroup not found!";
                        storage.removeGroupPermission(groupID, permission);
                        return "§6Permission §c" + permission + " §6removed from group §a" + groupID;
                    }
                    case "add-parent": {
                        if (args.length != 4) return "§6/" + label +" group add-parent <groupID> <parentGroupID> - add a specify parent group.";
                        String groupID = args[2].toLowerCase();
                        String parent = args[3].toLowerCase();
                        Group group = storage.getGroup(groupID);
                        if (group == null) return "§cGroup not found!";
                        Group parentGroup = storage.getGroup(parent);
                        if (parentGroup == null) return "§cParent group not found!";
                        storage.addGroupParent(groupID, parent);
                        return "§6Parent group §c" + parent + " §6added to group §a" + groupID;
                    }
                    case "remove-parent": {
                        if (args.length != 4) return "§6/" + label +" group remove-parent <groupID> <parentGroupID> - remove a specify parent group.";
                        String groupID = args[2].toLowerCase();
                        String parent = args[3].toLowerCase();
                        Group group = storage.getGroup(groupID);
                        if (group == null) return "§cGroup not found!";
                        storage.removeGroupParent(groupID, parent);
                        return "§6Parent group §c" + parent + " §6removed from group §a" + groupID;
                    }
                    case "delete": {
                        if (args.length != 3) return "§6/" + label +" group delete <groupID> - delete specify group.";
                        String groupID = args[2].toLowerCase();
                        Group group = storage.getGroup(groupID);
                        if (group == null) return "§cGroup not found!";
                        storage.deleteGroup(groupID);
                        return "§6Group §c" + groupID + " §6deleted!";
                    }
                    case "create": {
                        if (args.length != 3) return "§6/" + label +" group create <groupID> - create specify group.";
                        String groupID = args[2].toLowerCase();
                        Group group = storage.getGroup(groupID);
                        if (group != null) return "§cGroup is already exist!";
                        storage.createGroup(groupID);
                        return "§6Group §c" + groupID + " §6created!";
                    }
                    case "prefix": {
                        if (args.length < 4) return "§6/" + label +" group prefix <groupID> <prefix> - set group prefix.";
                        String groupID = args[2].toLowerCase();
                        Group group = storage.getGroup(groupID);
                        if (group == null) return "§cGroup not found!";
                        String prefix = this.getFinalArg(args, 3);
                        storage.setGroupPrefix(groupID, prefix);
                        return "§6Prefix §c" + prefix + " §6set to group §a" + groupID;
                    }
                    case "suffix": {
                        if (args.length < 4) return "§6/" + label +" group suffix <groupID> <prefix> - set group prefix.";
                        String groupID = args[2].toLowerCase();
                        Group group = storage.getGroup(groupID);
                        if (group == null) return "§cGroup not found!";
                        String suffix = this.getFinalArg(args, 3);
                        storage.setGroupPrefix(groupID, suffix);
                        return "§6Suffix §c" + suffix + " §6set to group §a" + groupID;
                    }
                    case "clear-prefix": {
                        if (args.length != 3) return "§6/" + label +" user clear-prefix <groupID> - clear group prefix.";
                        String groupID = args[2].toLowerCase();
                        Group group = storage.getGroup(groupID);
                        if (group == null) return "§cGroup not found!";
                        storage.setGroupPrefix(groupID, "");
                        return "§6Prefix remove from group §a" + groupID;
                    }
                    case "clear-suffix": {
                        if (args.length != 3) return "§6/" + label +" user clear-suffix <groupID> - clear group suffix.";
                        String groupID = args[2].toLowerCase();
                        Group group = storage.getGroup(groupID);
                        if (group == null) return "§cGroup not found!";
                        storage.setGroupSuffix(groupID, "");
                        return "§6Suffix remove from group §a" + groupID;
                    }
                    case "setoption": {
                        if (args.length != 4) return "§6/" + label +" group setoption <groupID> <option> - set specify group option.";
                        return "§cTODO setoption method";
                    }
                    case "removeoption": {
                        if (args.length != 4) return "§6/" + label +" group removeoption <groupID> <option> - remove specify group option.";
                        return "§cTODO removeoption method";
                    }
                    case "getoption": {
                        if (args.length != 4) return "§6/" + label +" group getoption <groupID> <option> - get specify group option.";

                        return "§cTODO getoption method";
                    }
                }
                break;
            }
            case "reload": {
                //todo reload method
                return "§aMinePerms reload successful!";
            }
            case "bulkupdate": {
                return "§cTODO bulka help";
            }
            case "export": {
                if (args.length != 4) return "§6/" + label +" export <from> <to> - export data from another backend.";

            }
        }
        return "§cUnknown operation.";
    }

    private String getFinalArg(String[] args, int start) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < args.length; ++i) {
            if (i != start) {
                builder.append(" ");
            }
            builder.append(args[i]);
        }
        return builder.toString();
    }

    private String listMessages(String... messages) {
        return Joiner.on('\n').join(messages);
    }
}
