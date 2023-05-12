package ua.klesaak.mineperms.manager.command;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import ua.klesaak.mineperms.MinePermsManager;
import ua.klesaak.mineperms.manager.storage.Group;
import ua.klesaak.mineperms.manager.storage.Storage;
import ua.klesaak.mineperms.manager.storage.User;

import java.util.*;

public class MinePermsCommand {
    public static final String MAIN_PERMISSION = "mineperms.admin";

    public static final List<String> SUB_COMMANDS_0 = Arrays.asList("user", "group", "reload", "bulkupdate", "export");
    public static final List<String> USER_SUB_COMMANDS_0 = Arrays.asList("addperm", "removeperm", "info", "setgroup", "delete", "prefix",
            "suffix", "clear-prefix", "clear-suffix");
    public static final List<String> GROUP_SUB_COMMANDS_0 = Arrays.asList("addperm", "removeperm", "info", "delete", "prefix",
            "suffix", "clear-prefix", "clear-suffix", "create", "add-parent", "remove-parent");

    public static final List<String> BULK_UPDATE_SUB_COMMANDS_0 = Arrays.asList("user", "group", "all");

    public static final List<String> EXPORT_SUB_COMMANDS = Arrays.asList("file", "mysql", "redis");
    private final MinePermsManager manager;

    public MinePermsCommand(MinePermsManager manager) {
        this.manager = manager;
    }

    public void invoke(MPCommandSource commandSource, String label, String[] args) {
        if (args.length == 0) {
            commandSource.sendMessage("§6MinePerms by Klesaak §cv1.0");
            commandSource.sendMessage("");
            commandSource.sendMessage("§6/" + label + " user - user operations command.");
            commandSource.sendMessage("§6/" + label + " group - group operations command.");
            commandSource.sendMessage("§6/" + label + " bulkupdate - bulkupdate operations command.");
            commandSource.sendMessage("§6/" + label + " export <from> <to> - export data from another backend.");
            return;
        }
        Storage storage = this.manager.getStorage();
        switch (args[0].toLowerCase()) {
            case "user": {
                if (args.length == 1) {
                    commandSource.sendMessage("§6/" + label +" user info <nickname> - show info of a player.");
                    commandSource.sendMessage("§6/" + label +" user addperm <nickname> <permission> - add a specify permission.");
                    commandSource.sendMessage("§6/" + label +" user removeperm <nickname> <permission> - remove specify permission.");
                    commandSource.sendMessage("§6/" + label +" user setgroup <nickname> <groupID> - set a specify group.");
                    commandSource.sendMessage("§6/" + label +" user delete <nickname> - delete specify user.");
                    commandSource.sendMessage("§6/" + label +" user prefix <nickname> <prefix> - set user prefix.");
                    commandSource.sendMessage("§6/" + label +" user suffix <nickname> <suffix> - set user suffix.");
                    commandSource.sendMessage("§6/" + label +" user clear-prefix <nickname> - clear user prefix.");
                    commandSource.sendMessage("§6/" + label +" user clear-suffix <nickname> - clear user suffix.");
                    return;
                }
                switch (args[1].toLowerCase()) {
                    case "info": {
                        if (args.length != 3) {
                            commandSource.sendMessage("§6/" + label +" user info <nickname> - show info of a player.");
                            return;
                        }
                        String nickName = args[2];
                        User user = this.manager.getStorage().getUser(nickName);
                        if (user == null) {
                            commandSource.sendMessage("§cUser not found!");
                            return;
                        }
                        commandSource.sendMessage("§aUser " + nickName + " info:");
                        commandSource.sendMessage(" §aGroup: §6" + user.getGroup());
                        commandSource.sendMessage(" §aPrefix: §6" + (user.getPrefix().isEmpty() ? "§cNot set." : user.getPrefix()));
                        commandSource.sendMessage(" §aSuffix: §6" + (user.getSuffix().isEmpty() ? "§cNot set." : user.getSuffix()));
                        if (user.getPermissions().isEmpty()) {
                            commandSource.sendMessage(" §aPermissions: §cPermissions not set!");
                            return;
                        }
                        commandSource.sendMessage(" §aPermissions:");
                        user.getPermissions().forEach(permission -> commandSource.sendMessage("  §a- " + permission));
                        return;
                    }
                    case "addperm": {
                        if (args.length != 4) {
                            commandSource.sendMessage("§6/" + label +" user addperm <nickname> <permission> - add a specify permission.");
                            return;
                        }
                        String nickName = args[2];
                        String permission = args[3];
                        if (this.checkSuperPermission(commandSource, permission)) return;
                        storage.addUserPermission(nickName, permission);
                        commandSource.sendMessage("§6Permission §c" + permission + " §6added to §a" + nickName);
                        return;
                    }
                    case "removeperm": {
                        if (args.length != 4) {
                            commandSource.sendMessage("§6/" + label +" user removeperm <nickname> <permission> - remove a specify permission.");
                            return;
                        }
                        String nickName = args[2];
                        String permission = args[3];
                        storage.removeUserPermission(nickName, permission);
                        commandSource.sendMessage("§6Permission §c" + permission + " §6removed from §a" + nickName);
                        return;
                    }
                    case "setgroup": {
                        if (args.length != 4) {
                            commandSource.sendMessage("§6/" + label +" user setgroup <nickname> <groupID> - set a specify group.");
                            return;
                        }
                        String nickName = args[2];
                        String groupID = args[3].toLowerCase();
                        Group group = storage.getGroup(groupID);
                        if (group == null) {
                            commandSource.sendMessage("§cGroup not found!");
                            return;
                        }
                        storage.setUserGroup(nickName, groupID);
                        commandSource.sendMessage("§6Group §c" + groupID + " §6set to §a" + nickName);
                        return;
                    }
                    case "delete": {
                        if (args.length != 3) {
                            commandSource.sendMessage("§6/" + label +" user delete <nickname> - delete specify user.");
                            return;
                        }
                        String nickName = args[2];
                        storage.deleteUser(nickName);
                        commandSource.sendMessage("§6User §c" + nickName + " §6deleted!");
                        return;
                    }
                    case "prefix": {
                        if (args.length < 4) {
                            commandSource.sendMessage("§6/" + label +" user prefix <nickname> <prefix> - set user prefix.");
                            return;
                        }
                        String nickName = args[2];
                        String prefix = this.getFinalArg(args, 3);
                        storage.setUserPrefix(nickName, prefix);
                        commandSource.sendMessage("§6Prefix §c" + prefix + " §6set to §a" + nickName);
                        return;
                    }
                    case "suffix": {
                        if (args.length < 4) {
                            commandSource.sendMessage("§6/" + label +" user suffix <nickname> <prefix> - set user suffix.");
                            return;
                        }
                        String nickName = args[2];
                        String suffix = this.getFinalArg(args, 3);
                        storage.setUserSuffix(nickName, suffix);
                        commandSource.sendMessage("§6Suffix §c" + suffix + " §6set to §a" + nickName);
                        return;
                    }
                    case "clear-prefix": {
                        if (args.length != 3) {
                            commandSource.sendMessage("§6/" + label +" user clear-prefix <nickname> - clear user prefix.");
                            return;
                        }
                        String nickName = args[2];
                        storage.setUserPrefix(nickName, "");
                        commandSource.sendMessage("§6Prefix remove from §a" + nickName);
                        return;
                    }
                    case "clear-suffix": {
                        if (args.length != 3) {
                            commandSource.sendMessage("§6/" + label +" user clear-suffix <nickname> - clear user suffix.");
                            return;
                        }
                        String nickName = args[2];
                        storage.setUserSuffix(nickName, "");
                        commandSource.sendMessage("§6Suffix remove from §a" + nickName);
                        return;
                    }
                }
                break;
            }
            case "group": {
                if (args.length == 1) {
                    commandSource.sendMessage("§6/" + label +" group info <groupID> - show info of a group.");
                    commandSource.sendMessage("§6/" + label +" group addperm <groupID> <permission> - add a specify permission.");
                    commandSource.sendMessage("§6/" + label +" group removeperm <groupID> <permission> - remove specify permission.");
                    commandSource.sendMessage("§6/" + label +" group add-parent <groupID> <parentGroupID> - add a specify parent group.");
                    commandSource.sendMessage("§6/" + label +" group remove-parent <groupID> <parentGroupID> - remove a specify parent group.");
                    commandSource.sendMessage("§6/" + label +" group delete <groupID> - delete specify group.");
                    commandSource.sendMessage("§6/" + label +" group create <groupID> - create specify group.");
                    commandSource.sendMessage("§6/" + label +" group prefix <groupID> <prefix> - set group prefix.");
                    commandSource.sendMessage("§6/" + label +" group suffix <groupID> <suffix> - set group suffix.");
                    commandSource.sendMessage("§6/" + label +" group clear-prefix <groupID> - clear group prefix.");
                    commandSource.sendMessage("§6/" + label +" group clear-suffix <groupID> - clear group suffix.");
                    return;
                }
                switch (args[1].toLowerCase()) {
                    case "info": {
                        if (args.length != 3) {
                            commandSource.sendMessage("§6/" + label +" group info <groupID> - show info of a group.");
                            return;
                        }
                        String groupID = args[2];
                        Group group = storage.getGroup(groupID);
                        if (group == null) {
                            commandSource.sendMessage("§cGroup not found!");
                            return;
                        }
                        String parents = group.getInheritanceGroups().isEmpty() ? "§cParents not set!" : Joiner.on(", ").join(group.getInheritanceGroups());
                        commandSource.sendMessage("§aGroup " + groupID + " info:");
                        commandSource.sendMessage(" §aPrefix: §6" + (group.getPrefix().isEmpty() ? "§cNot set." : group.getPrefix()));
                        commandSource.sendMessage(" §aSuffix: §6" + (group.getSuffix().isEmpty() ? "§cNot set." : group.getSuffix()));
                        commandSource.sendMessage(" §aParent groups: §6" + parents);
                        if (group.getPermissions().isEmpty()) {
                            commandSource.sendMessage(" §aPermissions: §cPermissions not set!");
                            return;
                        }
                        commandSource.sendMessage(" §aPermissions:");
                        group.getPermissions().forEach(permission -> commandSource.sendMessage("  §a- " + permission));
                        return;
                    }
                    case "addperm": {
                        if (args.length != 4) {
                            commandSource.sendMessage("§6/" + label +" group addperm <groupID> <permission> - add a specify permission.");
                            return;
                        }
                        String groupID = args[2].toLowerCase();
                        String permission = args[3];
                        Group group = storage.getGroup(groupID);
                        if (group == null) {
                            commandSource.sendMessage("§cGroup not found!");
                            return;
                        }
                        if (this.checkSuperPermission(commandSource, permission)) return;
                        storage.addGroupPermission(groupID, permission);
                        commandSource.sendMessage("§6Permission §c" + permission + " §6added to group §a" + groupID);
                        return;
                    }
                    case "removeperm": {
                        if (args.length != 4) {
                            commandSource.sendMessage("§6/" + label +" group removeperm <groupID> <permission> - remove a specify permission.");
                            return;
                        }
                        String groupID = args[2].toLowerCase();
                        String permission = args[3];
                        Group group = storage.getGroup(groupID);
                        if (group == null) {
                            commandSource.sendMessage("§cGroup not found!");
                            return;
                        }
                        storage.removeGroupPermission(groupID, permission);
                        commandSource.sendMessage("§6Permission §c" + permission + " §6removed from group §a" + groupID);
                        return;
                    }
                    case "add-parent": {
                        if (args.length != 4) {
                            commandSource.sendMessage("§6/" + label +" group add-parent <groupID> <parentGroupID> - add a specify parent group.");
                            return;
                        }
                        String groupID = args[2].toLowerCase();
                        String parent = args[3].toLowerCase();
                        Group group = storage.getGroup(groupID);
                        if (group == null) {
                            commandSource.sendMessage("§cGroup not found!");
                            return;
                        }
                        Group parentGroup = storage.getGroup(parent);
                        if (parentGroup == null) {
                            commandSource.sendMessage("§cParent group not found!");
                            return;
                        }
                        storage.addGroupParent(groupID, parent);
                        commandSource.sendMessage("§6Parent group §c" + parent + " §6added to group §a" + groupID);
                        return;
                    }
                    case "remove-parent": {
                        if (args.length != 4) {
                            commandSource.sendMessage("§6/" + label +" group remove-parent <groupID> <parentGroupID> - remove a specify parent group.");
                            return;
                        }
                        String groupID = args[2].toLowerCase();
                        String parent = args[3].toLowerCase();
                        Group group = storage.getGroup(groupID);
                        if (group == null) {
                            commandSource.sendMessage("§cGroup not found!");
                            return;
                        }
                        storage.removeGroupParent(groupID, parent);
                        commandSource.sendMessage("§6Parent group §c" + parent + " §6removed from group §a" + groupID);
                        return;
                    }
                    case "delete": {
                        if (args.length != 3) {
                            commandSource.sendMessage("§6/" + label +" group delete <groupID> - delete specify group.");
                            return;
                        }
                        String groupID = args[2].toLowerCase();
                        Group group = storage.getGroup(groupID);
                        if (group == null) {
                            commandSource.sendMessage("§cGroup not found!");
                            return;
                        }
                        storage.deleteGroup(groupID);
                        commandSource.sendMessage("§6Group §c" + groupID + " §6deleted!");
                        return;
                    }
                    case "create": {
                        if (args.length != 3) {
                            commandSource.sendMessage("§6/" + label +" group create <groupID> - create specify group.");
                            return;
                        }
                        String groupID = args[2].toLowerCase();
                        Group group = storage.getGroup(groupID);
                        if (group != null) {
                            commandSource.sendMessage("§cGroup is already exist!");
                            return;
                        }
                        storage.createGroup(groupID);
                        commandSource.sendMessage("§6Group §c" + groupID + " §6created!");
                        return;
                    }
                    case "prefix": {
                        if (args.length < 4) {
                            commandSource.sendMessage("§6/" + label +" group prefix <groupID> <prefix> - set group prefix.");
                            return;
                        }
                        String groupID = args[2].toLowerCase();
                        Group group = storage.getGroup(groupID);
                        if (group == null) {
                            commandSource.sendMessage("§cGroup not found!");
                            return;
                        }
                        String prefix = this.getFinalArg(args, 3);
                        storage.setGroupPrefix(groupID, prefix);
                        commandSource.sendMessage("§6Prefix §c" + prefix + " §6set to group §a" + groupID);
                        return;
                    }
                    case "suffix": {
                        if (args.length < 4) {
                            commandSource.sendMessage("§6/" + label +" group suffix <groupID> <prefix> - set group prefix.");
                            return;
                        }
                        String groupID = args[2].toLowerCase();
                        Group group = storage.getGroup(groupID);
                        if (group == null) {
                            commandSource.sendMessage("§cGroup not found!");
                            return;
                        }
                        String suffix = this.getFinalArg(args, 3);
                        storage.setGroupPrefix(groupID, suffix);
                        commandSource.sendMessage("§6Suffix §c" + suffix + " §6set to group §a" + groupID);
                        return;
                    }
                    case "clear-prefix": {
                        if (args.length != 3) {
                            commandSource.sendMessage("§6/" + label +" user clear-prefix <groupID> - clear group prefix.");
                            return;
                        }
                        String groupID = args[2].toLowerCase();
                        Group group = storage.getGroup(groupID);
                        if (group == null) {
                            commandSource.sendMessage("§cGroup not found!");
                            return;
                        }
                        storage.setGroupPrefix(groupID, "");
                        commandSource.sendMessage("§6Prefix remove from group §a" + groupID);
                        return;
                    }
                    case "clear-suffix": {
                        if (args.length != 3) {
                            commandSource.sendMessage("§6/" + label +" user clear-suffix <groupID> - clear group suffix.");
                            return;
                        }
                        String groupID = args[2].toLowerCase();
                        Group group = storage.getGroup(groupID);
                        if (group == null) {
                            commandSource.sendMessage("§cGroup not found!");
                            return;
                        }
                        storage.setGroupSuffix(groupID, "");
                        commandSource.sendMessage("§6Suffix remove from group §a" + groupID);
                        return;
                    }
                }
                break;
            }
            case "reload": {
                //todo reload method
                commandSource.sendMessage("§aMinePerms reload successful!");
                return;
            }
            case "bulkupdate": {
                commandSource.sendMessage("§cTODO bulka help");//todo
                return;
            }
            case "export": {
                if (args.length != 4)  {
                    commandSource.sendMessage("§6/" + label +" export <from> <to> - export data from another backend.");
                    return;
                }
                //todo
            }
        }
        commandSource.sendMessage("§cUnknown operation.");
    }

    public List<String> onTabComplete(String label, Collection<String> onlinePlayers, String[] args) {
        if (args.length == 1) {
            return this.copyPartialMatches(args[0].toLowerCase(), SUB_COMMANDS_0, new ArrayList<>());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("user")) {
            return this.copyPartialMatches(args[1].toLowerCase(), USER_SUB_COMMANDS_0, new ArrayList<>());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("group")) {
            return this.copyPartialMatches(args[1].toLowerCase(), GROUP_SUB_COMMANDS_0, new ArrayList<>());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("user")) {
            return this.copyPartialMatches(args[2].toLowerCase(), onlinePlayers, new ArrayList<>());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("group")) {
            return this.copyPartialMatches(args[2].toLowerCase(), this.manager.getStorage().getGroupNames(), new ArrayList<>());
        }
        //todo tab for bulkupdate
        if (args.length == 2 && args[0].equalsIgnoreCase("export")) {
            return this.copyPartialMatches(args[1].toLowerCase(), EXPORT_SUB_COMMANDS, new ArrayList<>());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("export")) {
            return this.copyPartialMatches(args[2].toLowerCase(), EXPORT_SUB_COMMANDS, new ArrayList<>());
        }
        return Collections.emptyList();
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

    private <T extends Collection<? super String>> T copyPartialMatches(String token, Iterable<String> originals, T collection) {
        Preconditions.checkNotNull(token, "Search token cannot be null");
        Preconditions.checkNotNull(collection, "Collection cannot be null");
        Preconditions.checkNotNull(originals, "Originals cannot be null");
        originals.forEach(string -> { if (startsWithIgnoreCase(string, token)) collection.add(string);});
        return collection;
    }

    private boolean startsWithIgnoreCase(String string, String prefix) {
        Preconditions.checkNotNull(string, "Cannot check a null string for a match");
        return string.length() >= prefix.length() && string.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    private boolean checkSuperPermission(MPCommandSource commandSource, String permission) {
        if (permission.equals(MinePermsManager.ROOT_WILDCARD) && !commandSource.hasPermission(MinePermsManager.ROOT_WILDCARD)) {
            commandSource.sendMessage("§cYou can't add super-permission because you don't have it!");
            return true;
        }
        return false;
    }
}
