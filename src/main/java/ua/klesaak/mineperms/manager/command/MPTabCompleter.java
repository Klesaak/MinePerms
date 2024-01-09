package ua.klesaak.mineperms.manager.command;

import com.google.common.base.Preconditions;
import ua.klesaak.mineperms.manager.storage.StorageType;

import java.util.*;

abstract class MPTabCompleter {
    public static final List<String> SUB_COMMANDS_0 = Arrays.asList("user", "group", "find", "export", "migrate", "test-perm");
    public static final List<String> USER_SUB_COMMANDS_0 = Arrays.asList("add-perm", "remove-perm", "info", "permissions-info", "set-group", "delete", "prefix",
            "suffix", "clear-prefix", "clear-suffix");
    public static final List<String> GROUP_SUB_COMMANDS_0 = Arrays.asList("add-perm", "remove-perm", "info", "permissions-info", "delete", "prefix",
            "suffix", "clear-prefix", "clear-suffix", "create", "add-parent", "remove-parent");

    public static final List<String> FIND_SUB_COMMANDS_0 = Arrays.asList("user", "group", "all");
    public static final List<String> FIND_SUB_COMMANDS_1 = Arrays.asList("permission", "parent-group");

    public static final List<String> EXPORT_SUB_COMMANDS = new ArrayList<>(StorageType.getTypesString());
    public static final List<String> MIGRATE_SUB_COMMANDS = Arrays.asList("simpleperms", "pex", "luckperms");

    public List<String> suggestMainCommand(String[] args) {
        return this.copyPartialMatches(args[0].toLowerCase(), SUB_COMMANDS_0, new ArrayList<>());
    }

    public List<String> suggestUserCommand(Collection<String> onlinePlayers, List<String> groupNames, String[] args) {
        switch (args.length) {
            case 2: {
                return this.copyPartialMatches(args[1].toLowerCase(), onlinePlayers, new ArrayList<>());
            }
            case 3: {
                return this.copyPartialMatches(args[2].toLowerCase(), USER_SUB_COMMANDS_0, new ArrayList<>());
            }
            case 4: {
                if (args[2].equalsIgnoreCase("set-group")) {
                    return this.copyPartialMatches(args[3].toLowerCase(), groupNames, new ArrayList<>());
                }
            }
        }
        return Collections.emptyList();
    }

    public List<String> suggestGroupCommand(List<String> groupNames, String[] args) {
        switch (args.length) {
            case 2: {
                return this.copyPartialMatches(args[1].toLowerCase(), groupNames, new ArrayList<>());
            }
            case 3: {
                return this.copyPartialMatches(args[2].toLowerCase(), GROUP_SUB_COMMANDS_0, new ArrayList<>());
            }
            case 4: {
                if (args[2].equalsIgnoreCase("add-parent")) {
                    return this.copyPartialMatches(args[3].toLowerCase(), groupNames, new ArrayList<>());
                }
            }
        }
        return Collections.emptyList();
    }

    public List<String> suggestFindCommand(String[] args) {
        switch (args.length) {
            case 2: {
                return this.copyPartialMatches(args[1].toLowerCase(), FIND_SUB_COMMANDS_0, new ArrayList<>());
            }
            case 3: {
                return this.copyPartialMatches(args[2].toLowerCase(), FIND_SUB_COMMANDS_1, new ArrayList<>());
            }
        }
        return Collections.emptyList();
    }

    public List<String> suggestExportCommand(String[] args) {
        if (args.length == 2) {
            return this.copyPartialMatches(args[1].toLowerCase(), EXPORT_SUB_COMMANDS, new ArrayList<>());
        }
        return Collections.emptyList();
    }

    public List<String> suggestMigrateCommand(String[] args) {
        if (args.length == 2) {
            return this.copyPartialMatches(args[1].toLowerCase(), MIGRATE_SUB_COMMANDS, new ArrayList<>());
        }
        return Collections.emptyList();
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
}
