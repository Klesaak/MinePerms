package ua.klesaak.mineperms.manager.command;

import com.google.common.base.Joiner;
import lombok.val;
import ua.klesaak.mineperms.MinePermsManager;
import ua.klesaak.mineperms.manager.migration.IMigrationPlugin;
import ua.klesaak.mineperms.manager.migration.LPMigration;
import ua.klesaak.mineperms.manager.migration.PEXMigration;
import ua.klesaak.mineperms.manager.migration.SpermMigration;
import ua.klesaak.mineperms.manager.storage.Storage;
import ua.klesaak.mineperms.manager.storage.StorageType;
import ua.klesaak.mineperms.manager.storage.entity.Group;
import ua.klesaak.mineperms.manager.storage.entity.User;
import ua.klesaak.mineperms.manager.storage.file.FileStorage;
import ua.klesaak.mineperms.manager.storage.sql.SQLStorage;
import ua.klesaak.mineperms.manager.utils.Paginated;
import ua.klesaak.mineperms.manager.utils.PermissionsMatcher;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class MinePermsCommand extends MPTabCompleter {
    public static final String MAIN_PERMISSION = "mineperms.admin";
    private final MinePermsManager manager;
    private final ReadWriteLock highOperationsLock = new ReentrantReadWriteLock();

    public MinePermsCommand(MinePermsManager manager) {
        this.manager = manager;
    }

    public void onExecute(IMPCommandSource commandSource, String label, String[] args) {
        if (args.length == 0) {
            commandSource.sendMessage("&6MinePerms by Klesaak &cv1.0 - BETA");
            commandSource.sendMessage("");
            commandSource.sendMessage("&6/" + label + " user - user operations command.");
            commandSource.sendMessage("&6/" + label + " group - group operations command.");
            commandSource.sendMessage("&6/" + label + " find <group|user|all> <permission|parent-group> <identifier> - find user/group with special permission/group command.");
            commandSource.sendMessage("&6/" + label + " export <backend> - export data from current backend to another backend.");
            commandSource.sendMessage("&6/" + label + " migrate <simpleperms|pex|luckperms> - migrate data from another perm-plugin to current backend.");
            commandSource.sendMessage("&6/" + label + " test-perm <permission> - show info of permission which you have.");
            //commandSource.sendMessage("");
            //commandSource.sendMessage("&6Temporal cache size: &c" + this.manager.getStorage().getTemporalUsersCache().size());
            //commandSource.sendMessage("&6Temporal cache keys: &c" + new HashSet<>(this.manager.getStorage().getTemporalUsersCache().asMap().keySet()));
            //commandSource.sendMessage("&6Cache size: &c" + this.manager.getStorage().getUsers().size());
            //commandSource.sendMessage("&6Is Klesaak contains: &c" + this.manager.getStorage().getTemporalUsersCache().containsKey("klesaak"));
            return;
        }
        Storage storage = this.manager.getStorage();
        switch (args[0].toLowerCase()) {
            case "test-perm": {
                if (args.length != 2) {
                    commandSource.sendMessage("&6/" + label +" test-perm <permission> - show info of permission which you have.");
                    return;
                }
                String permission = args[1];
                long startTime = System.nanoTime();
                boolean hasPerm = commandSource.hasPermission(permission);
                long completeTime = System.nanoTime() - startTime;
                commandSource.sendMessage("&6Permission: &c" + permission + " &6== " + (hasPerm ? "&a" : "&c") + hasPerm +" &6(" + this.spaced((int)completeTime, " ") + ")ns");
                return;
            }
            case "user": {
                if (args.length < 3) {
                    commandSource.sendMessage("&6MinePerms User command help:");
                    commandSource.sendMessage("");
                    commandSource.sendMessage("&6/" + label + " user <nickname> info - show info of a player.");
                    commandSource.sendMessage("&6/" + label + " user <nickname> permissions-info - show info of a user permissions.");
                    commandSource.sendMessage("&6/" + label + " user <nickname> add-perm <permission> - add a specify permission.");
                    commandSource.sendMessage("&6/" + label + " user <nickname> remove-perm <permission> - remove specify permission.");
                    commandSource.sendMessage("&6/" + label + " user <nickname> set-group <groupId> - set a specify group.");
                    commandSource.sendMessage("&6/" + label + " user <nickname> delete - delete specify user.");
                    commandSource.sendMessage("&6/" + label + " user <nickname> prefix <prefix> - set user prefix.");
                    commandSource.sendMessage("&6/" + label + " user <nickname> suffix <suffix> - set user suffix.");
                    commandSource.sendMessage("&6/" + label + " user <nickname> clear-prefix - clear user prefix.");
                    commandSource.sendMessage("&6/" + label + " user <nickname> clear-suffix - clear user suffix.");
                    return;
                }
                String nickName = args[1];
                switch (args[2].toLowerCase()) {
                    case "info": {
                        if (args.length != 3) {
                            commandSource.sendMessage("&6/" + label +" user <nickname> info - show info of a player.");
                            return;
                        }
                        User user = storage.getUser(nickName);
                        if (user == null) {
                            commandSource.sendMessage("&cUser not found!");
                            return;
                        }
                        commandSource.sendMessage("&aUser &c" + nickName + "&a info:");
                        commandSource.sendMessage(" &aGroup: &6" + user.getGroupId());
                        commandSource.sendMessage(" &aPrefix: &6" + (user.getPrefix().isEmpty() ? "&cNot set." : user.getPrefix()));
                        commandSource.sendMessage(" &aSuffix: &6" + (user.getSuffix().isEmpty() ? "&cNot set." : user.getSuffix()));
                        return;
                    }
                    case "permissions-info": {
                        User user = storage.getUser(nickName);
                        if (user == null) {
                            commandSource.sendMessage("&cUser not found!");
                            return;
                        }
                        if (user.getPermissions().isEmpty()) {
                            commandSource.sendMessage("&cUser "+ nickName + " permissions not set!");
                            return;
                        }
                        val permsPages = new Paginated<>(user.getPermissions());
                        int maxPages = permsPages.getMaxPages(20);
                        int page = 1;
                        try {
                            page = this.parsePage(commandSource, args[3]);
                        } catch (IndexOutOfBoundsException ignored) {
                        }
                        if (page > maxPages) page = 1;
                        commandSource.sendMessage("&aUser &c" + nickName + "&a permissions: (page " + page + " of " + maxPages + " - " + user.getPermissions().size() + " entries):");
                        permsPages.getPage(page, 20).forEach(entry -> commandSource.sendMessage("  &6- " + entry.value()));
                        return;
                    }
                    case "add-perm": {
                        if (args.length != 4) {
                            commandSource.sendMessage("&6/" + label +" user <nickname> add-perm <permission> - add a specify permission.");
                            return;
                        }
                        String permission = args[3];
                        if (this.checkSuperPermission(commandSource, permission)) return;
                        if (this.checkAsteriskPermission(commandSource, () -> storage.addUserPermission(nickName, permission))) return;
                        commandSource.sendMessage("&6Permission &c" + permission + " &6added to &a" + nickName);
                        return;
                    }
                    case "remove-perm": {
                        if (args.length != 4) {
                            commandSource.sendMessage("&6/" + label +" user <nickname> remove-perm <permission> - remove a specify permission.");
                            return;
                        }
                        String permission = args[3];
                        storage.removeUserPermission(nickName, permission);
                        commandSource.sendMessage("&6Permission &c" + permission + " &6removed from &a" + nickName);
                        return;
                    }
                    case "set-group": {
                        if (args.length != 4) {
                            commandSource.sendMessage("&6/" + label +" user <nickname> set-group <groupId> - set a specify group.");
                            return;
                        }
                        String groupId = args[3].toLowerCase();
                        Group group = storage.getGroup(groupId);
                        if (group == null) {
                            commandSource.sendMessage("&cGroup not found!");
                            return;
                        }
                        storage.setUserGroup(nickName, groupId);
                        commandSource.sendMessage("&6Group &c" + groupId + " &6set to &a" + nickName);
                        return;
                    }
                    case "delete": {
                        if (args.length != 3) {
                            commandSource.sendMessage("&6/" + label +" user <nickname> delete - delete specify user.");
                            return;
                        }
                        storage.deleteUser(nickName);
                        commandSource.sendMessage("&6User &c" + nickName + " &6deleted!");
                        return;
                    }
                    case "prefix": {
                        if (args.length < 4) {
                            commandSource.sendMessage("&6/" + label +" user <nickname> prefix <prefix> - set user prefix.");
                            return;
                        }

                        String prefix = this.getFinalArg(args, 3);
                        storage.setUserPrefix(nickName, prefix);
                        commandSource.sendMessage("&6Prefix &c" + prefix + " &6set to &a" + nickName);
                        return;
                    }
                    case "suffix": {
                        if (args.length < 4) {
                            commandSource.sendMessage("&6/" + label +" user <nickname> suffix <prefix> - set user suffix.");
                            return;
                        }
                        String suffix = this.getFinalArg(args, 3);
                        storage.setUserSuffix(nickName, suffix);
                        commandSource.sendMessage("&6Suffix &c" + suffix + " &6set to &a" + nickName);
                        return;
                    }
                    case "clear-prefix": {
                        if (args.length != 3) {
                            commandSource.sendMessage("&6/" + label +" user <nickname> clear-prefix - clear user prefix.");
                            return;
                        }
                        storage.setUserPrefix(nickName, "");
                        commandSource.sendMessage("&6Prefix removed from &a" + nickName);
                        return;
                    }
                    case "clear-suffix": {
                        if (args.length != 3) {
                            commandSource.sendMessage("&6/" + label +" user <nickname> clear-suffix - clear user suffix.");
                            return;
                        }
                        storage.setUserSuffix(nickName, "");
                        commandSource.sendMessage("&6Suffix removed from &a" + nickName);
                        return;
                    }
                }
                break;
            }
            case "group": {
                val groupsContext = this.manager.getConfigFile().getSQLSettings().getGroupsPermissionsTableSuffix();
                if (args.length < 3) {
                    commandSource.sendMessage("&6MinePerms Group command help:");
                    commandSource.sendMessage("");
                    commandSource.sendMessage("&6/" + label + " group <groupId> info - show info of a group.");
                    commandSource.sendMessage("&6/" + label + " group <groupId> permissions-info - show info of a group permissions.");
                    commandSource.sendMessage("&6/" + label + " group <groupId> add-perm <permission> - add a specify permission.");
                    commandSource.sendMessage("&6/" + label + " group <groupId> remove-perm <permission> - remove specify permission.");
                    commandSource.sendMessage("&6/" + label + " group <groupId> add-parent <parentGroupId> - add a specify parent group.");
                    commandSource.sendMessage("&6/" + label + " group <groupId> remove-parent <parentGroupId> - remove a specify parent group.");
                    commandSource.sendMessage("&6/" + label + " group <groupId> delete - delete specify group.");
                    commandSource.sendMessage("&6/" + label + " group <groupId> create - create specify group.");
                    commandSource.sendMessage("&6/" + label + " group <groupId> prefix <prefix> - set group prefix.");
                    commandSource.sendMessage("&6/" + label + " group <groupId> suffix <suffix> - set group suffix.");
                    commandSource.sendMessage("&6/" + label + " group <groupId> clear-prefix - clear group prefix.");
                    commandSource.sendMessage("&6/" + label + " group <groupId> clear-suffix - clear group suffix.");
                    return;
                }
                String groupId = args[1].toLowerCase();
                switch (args[2].toLowerCase()) {
                    case "info": {
                        if (args.length != 3) {
                            commandSource.sendMessage("&6/" + label + " group <groupId> info - show info of a group.");
                            return;
                        }
                        Group group = storage.getGroup(groupId);
                        if (group == null) {
                            commandSource.sendMessage("&cGroup not found!");
                            return;
                        }
                        String parents = group.getInheritanceGroups().isEmpty() ? "&cParents not set!" : Joiner.on(", ").join(group.getInheritanceGroups());
                        commandSource.sendMessage("&aGroup &c" + groupId + "&a info:");
                        commandSource.sendMessage(" &aPrefix: &6" + (group.getPrefix().isEmpty() ? "&cNot set." : group.getPrefix()));
                        commandSource.sendMessage(" &aSuffix: &6" + (group.getSuffix().isEmpty() ? "&cNot set." : group.getSuffix()));
                        commandSource.sendMessage(" &aParent groups: &6" + parents);
                        return;
                    }
                    case "permissions-info": {
                        Group group = storage.getGroup(groupId);
                        if (group == null) {
                            commandSource.sendMessage("&cGroup not found!");
                            return;
                        }
                        if (group.getPermissions().isEmpty()) {
                            commandSource.sendMessage("&cGroup "+ groupId +" permissions not set!");
                            return;
                        }
                        val permsPages = new Paginated<>(group.getPermissions());
                        int maxPages = permsPages.getMaxPages(20);
                        int page = 1;
                        try {
                            page = this.parsePage(commandSource, args[3]);
                        } catch (IndexOutOfBoundsException ignored) {
                        }
                        if (page > maxPages) page = 1;
                        if (this.manager.getStorageType().isSQL()) {
                            commandSource.sendMessage("&aGroup &c" + groupId + "&a permissions in context: &6" + groupsContext + "&a (page " + page + " of " + maxPages + " - " + group.getPermissions().size() + " entries):");
                            permsPages.getPage(page, 20).forEach(entry -> commandSource.sendMessage("  &6- " + entry.value()));
                            return;
                        }
                        commandSource.sendMessage("&aGroup &c" + groupId + "&a permissions: (page " + page + " of " + maxPages + " - " + group.getPermissions().size() + " entries):");
                        permsPages.getPage(page, 20).forEach(entry -> commandSource.sendMessage("  &6- " + entry.value()));
                        return;
                    }
                    case "add-perm": {
                        if (args.length != 4) {
                            commandSource.sendMessage("&6/" + label +" group <groupId> add-perm <permission> - add a specify permission.");
                            return;
                        }
                        String permission = args[3];
                        Group group = storage.getGroup(groupId);
                        if (group == null) {
                            commandSource.sendMessage("&cGroup not found!");
                            return;
                        }
                        if (this.checkSuperPermission(commandSource, permission)) return;
                        if (this.checkAsteriskPermission(commandSource, ()-> storage.addGroupPermission(groupId, permission))) return;
                        if (this.manager.getStorageType().isSQL()) {
                            commandSource.sendMessage("&6Permission &c" + permission + " &6added to group &a" + groupId + " &6in context: &c" + groupsContext);
                            return;
                        }
                        commandSource.sendMessage("&6Permission &c" + permission + " &6added to group &a" + groupId);
                        return;
                    }
                    case "remove-perm": {
                        if (args.length != 4) {
                            commandSource.sendMessage("&6/" + label +" group <groupId> remove-perm <permission> - remove a specify permission.");
                            return;
                        }
                        String permission = args[3];
                        Group group = storage.getGroup(groupId);
                        if (group == null) {
                            commandSource.sendMessage("&cGroup not found!");
                            return;
                        }
                        storage.removeGroupPermission(groupId, permission);
                        if (this.manager.getStorageType().isSQL()) {
                            commandSource.sendMessage("&6Permission &c" + permission + " &6removed from group &a" + groupId + " &6in context: &c" + this.manager.getConfigFile().getSQLSettings().getGroupsPermissionsTableSuffix());
                            return;
                        }
                        commandSource.sendMessage("&6Permission &c" + permission + " &6removed from group &a" + groupId);
                        return;
                    }
                    case "add-parent": {
                        if (args.length != 4) {
                            commandSource.sendMessage("&6/" + label +" group <groupId> add-parent <parentGroupId> - add a specify parent group.");
                            return;
                        }
                        String parent = args[3].toLowerCase();
                        Group group = storage.getGroup(groupId);
                        if (group == null) {
                            commandSource.sendMessage("&cGroup not found!");
                            return;
                        }
                        Group parentGroup = storage.getGroup(parent);
                        if (parentGroup == null) {
                            commandSource.sendMessage("&cParent group not found!");
                            return;
                        }
                        if (storage.getGroup(groupId).hasGroup(parent) || groupId.equalsIgnoreCase(parent)) {
                            commandSource.sendMessage("&6Parent group &c" + parent + "&6 already in &c" + groupId);
                            return;
                        }
                        storage.addGroupParent(groupId, parent);
                        commandSource.sendMessage("&6Parent group &c" + parent + " &6added to group &a" + groupId);
                        return;
                    }
                    case "remove-parent": {
                        if (args.length != 4) {
                            commandSource.sendMessage("&6/" + label +" group <groupId> remove-parent <parentGroupId> - remove a specify parent group.");
                            return;
                        }
                        String parent = args[3].toLowerCase();
                        Group group = storage.getGroup(groupId);
                        if (group == null) {
                            commandSource.sendMessage("&cGroup not found!");
                            return;
                        }
                        storage.removeGroupParent(groupId, parent);
                        commandSource.sendMessage("&6Parent group &c" + parent + " &6removed from group &a" + groupId);
                        return;
                    }
                    case "delete": {
                        if (args.length != 3) {
                            commandSource.sendMessage("&6/" + label +" group <groupId> delete - delete specify group.");
                            return;
                        }
                        Group group = storage.getGroup(groupId);
                        if (group == null) {
                            commandSource.sendMessage("&cGroup not found!");
                            return;
                        }
                        if (groupId.equalsIgnoreCase(storage.getDefaultGroup().getGroupId())) {
                            commandSource.sendMessage("&cYou can't delete default group!");
                            return;
                        }
                        storage.deleteGroup(groupId);
                        commandSource.sendMessage("&6Group &c" + groupId + " &6deleted!");
                        return;
                    }
                    case "create": {
                        if (args.length != 3) {
                            commandSource.sendMessage("&6/" + label +" group <groupId> create - create specify group.");
                            return;
                        }
                        Group group = storage.getGroup(groupId);
                        if (group != null) {
                            commandSource.sendMessage("&cGroup is already exist!");
                            return;
                        }
                        storage.createGroup(groupId);
                        commandSource.sendMessage("&6Group &c" + groupId + " &6created!");
                        return;
                    }
                    case "prefix": {
                        if (args.length < 4) {
                            commandSource.sendMessage("&6/" + label +" group <groupId> prefix <prefix> - set group prefix.");
                            return;
                        }
                        Group group = storage.getGroup(groupId);
                        if (group == null) {
                            commandSource.sendMessage("&cGroup not found!");
                            return;
                        }
                        String prefix = this.getFinalArg(args, 3);
                        storage.setGroupPrefix(groupId, prefix);
                        commandSource.sendMessage("&6Prefix &c" + prefix + " &6set to group &a" + groupId);
                        return;
                    }
                    case "suffix": {
                        if (args.length < 4) {
                            commandSource.sendMessage("&6/" + label +" group <groupId> suffix <prefix> - set group prefix.");
                            return;
                        }
                        Group group = storage.getGroup(groupId);
                        if (group == null) {
                            commandSource.sendMessage("&cGroup not found!");
                            return;
                        }
                        String suffix = this.getFinalArg(args, 3);
                        storage.setGroupSuffix(groupId, suffix);
                        commandSource.sendMessage("&6Suffix &c" + suffix + " &6set to group &a" + groupId);
                        return;
                    }
                    case "clear-prefix": {
                        if (args.length != 3) {
                            commandSource.sendMessage("&6/" + label +" user <groupId> clear-prefix - clear group prefix.");
                            return;
                        }
                        Group group = storage.getGroup(groupId);
                        if (group == null) {
                            commandSource.sendMessage("&cGroup not found!");
                            return;
                        }
                        storage.setGroupPrefix(groupId, "");
                        commandSource.sendMessage("&6Prefix removed from group &a" + groupId);
                        return;
                    }
                    case "clear-suffix": {
                        if (args.length != 3) {
                            commandSource.sendMessage("&6/" + label +" user <groupId> clear-suffix - clear group suffix.");
                            return;
                        }
                        Group group = storage.getGroup(groupId);
                        if (group == null) {
                            commandSource.sendMessage("&cGroup not found!");
                            return;
                        }
                        storage.setGroupSuffix(groupId, "");
                        commandSource.sendMessage("&6Suffix removed from group &a" + groupId);
                        return;
                    }
                }
                break;
            }
            case "find": {
                CompletableFuture.runAsync(()-> this.onFind(commandSource, label, args)).exceptionally(throwable -> {
                    commandSource.sendMessage("&cError while find data: " + throwable.getMessage());
                    return null;
                });
                return;
            }
            case "export": {
                CompletableFuture.runAsync(()-> this.onExport(commandSource, label, args)).exceptionally(throwable -> {
                    commandSource.sendMessage("&cError while export data: " + throwable.getMessage());
                    return null;
                });
                return;
            }
            case "migrate": {
                CompletableFuture.runAsync(()-> this.onMigrate(commandSource, label, args)).exceptionally(throwable -> {
                    commandSource.sendMessage("&cError while migrate data: " + throwable.getMessage());
                    return null;
                });
                return;
            }
        }
        commandSource.sendMessage("&cUnknown operation: &6" + args[args.length-1]);
    }

    private void onMigrate(IMPCommandSource commandSource, String label, String[] args) {
        if (args.length != 2)  {
            commandSource.sendMessage("&6/" + label + " migrate <simpleperms|pex|luckperms> - migrate data from another perm-plugin to current backend.");
            return;
        }
        val plugin = args[1].toLowerCase();
        long start = System.currentTimeMillis();
        IMigrationPlugin migrationPlugin;
        switch (plugin) {
            case "simpleperms": {
                migrationPlugin = new SpermMigration();
                break;
            }
            case "pex": {
                migrationPlugin = new PEXMigration();
                break;
            }
            case "luckperms": {
                migrationPlugin = new LPMigration();
                break;
            }
            default: {
                commandSource.sendMessage("&cERROR: Unknown plugin type: " + plugin);
                commandSource.sendMessage("&cAvailable plugins to migrating: &6" + Joiner.on(", ").join(MPTabCompleter.MIGRATE_SUB_COMMANDS));
                return;
            }
        }
        commandSource.sendMessage("&cStart migrating data...");
        this.runOnLock(commandSource, ()-> {
            val groupsCollection = migrationPlugin.getAllGroups();
            val storage = this.manager.getStorage();
            storage.importGroupsData(groupsCollection);
            val usersCollection = migrationPlugin.getAllUsers();
            storage.importUsersData(usersCollection);
            commandSource.sendMessage("&aMigrating complete! (" + (System.currentTimeMillis() - start) + "ms.)");
            commandSource.sendMessage("&aYou must delete old permission plugin and restart your server!");
        });
    }

    private void onExport(IMPCommandSource commandSource, String label, String[] args) {
        if (args.length != 2)  {
            commandSource.sendMessage("&6/" + label + " export <backend> - export data from current backend to another backend.");
            return;
        }
        val backend = args[1].toLowerCase();
        StorageType storageType;
        try {
            storageType = StorageType.parseWithException(backend);
        } catch (RuntimeException e) {
            commandSource.sendMessage("&cBackend &6" + backend + " &cis not exists! Available backends: &6FILE, MYSQL, MARIADB, POSTGRESQL");
            commandSource.sendMessage("&cCurrent backend: &6" + this.manager.getStorageType());
            return;
        }
        if (storageType == this.manager.getStorageType()) {
            commandSource.sendMessage("&cYou can't export data from current backend to current :-/");
            return;
        }
        long start = System.currentTimeMillis();
        commandSource.sendMessage("&cStart exporting data...");
        this.runOnLock(commandSource, ()-> {
            Storage newStorage;
            switch (storageType) {
                case MARIADB:
                case POSTGRESQL:
                case MYSQL: {
                    newStorage = new SQLStorage(this.manager, storageType);
                    break;
                }
                default: {
                    newStorage = new FileStorage(this.manager);
                    break;
                }

            }
            try (Storage storage = newStorage) {
                val currentStorage = this.manager.getStorage();
                val users = currentStorage.getAllUsersData();
                val groups = currentStorage.getAllGroupsData();
                storage.importGroupsData(groups);
                storage.importUsersData(users);
                commandSource.sendMessage("&aExporting complete! (" + (System.currentTimeMillis() - start) + "ms.)");
                commandSource.sendMessage("&aTo cross the &6" + storageType + "&a backend, you must change field 'storageType' in file config.json and restart you server!");
            } catch (Exception e) {
                throw new RuntimeException("Got error while export data", e);
            }
        });
    }

    private void onFind(IMPCommandSource commandSource, String label, String[] args) {
        if (args.length != 4) {
            commandSource.sendMessage("&6/" + label + " find <group|user|all> <permission|parent-group> <identifier> - find user/group with special permission/group command.");
            return;
        }
        this.runOnLock(commandSource, ()-> {
            switch (args[1].toLowerCase()) {
                case "group": {
                    long start = System.currentTimeMillis();
                    commandSource.sendMessage("&cStart finding...");
                    val groups = this.manager.getStorage().getAllGroupsData();
                    val identifier = args[3].toLowerCase();
                    val found = new ArrayList<String>();
                    switch (args[2].toLowerCase()) {
                        case "permission": {
                            for (val group : groups) {
                                if (group.hasOwnPermission(identifier)) found.add(group.getGroupId());
                            }
                            break;
                        }
                        case "parent-group": {
                            for (val group : groups) {
                                if (group.hasGroup(identifier)) found.add(group.getGroupId());
                            }
                            break;
                        }
                    }
                    commandSource.sendMessage("&aFinding Group's with " + args[2].toUpperCase() + "=" + identifier + " complete! (" + (System.currentTimeMillis() - start) + "ms.)");
                    commandSource.sendMessage(found.isEmpty() ? "&cNothing..." : "&aResults: &6" + Joiner.on(", ").join(found));
                    return;
                }
                case "user": {
                    long start = System.currentTimeMillis();
                    commandSource.sendMessage("&cStart finding...");
                    val users = this.manager.getStorage().getAllUsersData();
                    val identifier = args[3].toLowerCase();
                    val found = new ArrayList<String>();
                    switch (args[2].toLowerCase()) {
                        case "permission": {
                            for (val user : users) {
                                if (user.hasOwnPermission(identifier)) found.add(user.getPlayerName());
                            }
                            break;
                        }
                        case "parent-group": {
                            for (val user : users) {
                                if (user.hasGroup(identifier)) found.add(user.getPlayerName());
                            }
                            break;
                        }
                    }
                    commandSource.sendMessage("&aFinding User's with " + args[2].toUpperCase() + "=" + identifier + " complete! (" + (System.currentTimeMillis() - start) + "ms.)");
                    commandSource.sendMessage(found.isEmpty() ? "&cNothing..." : "&aResults: &6" + Joiner.on(", ").join(found));
                    return;
                }
                case "all": {
                    long start = System.currentTimeMillis();
                    commandSource.sendMessage("&cStart finding...");
                    val users = this.manager.getStorage().getAllUsersData();
                    val groups = this.manager.getStorage().getAllGroupsData();
                    val identifier = args[3].toLowerCase();
                    val usersFound = new ArrayList<String>();
                    val groupsFound = new ArrayList<String>();
                    switch (args[2].toLowerCase()) {
                        case "permission": {
                            for (val user : users) {
                                if (user.hasOwnPermission(identifier)) usersFound.add(user.getPlayerName());
                            }
                            for (val group : groups) {
                                if (group.hasOwnPermission(identifier)) groupsFound.add(group.getGroupId());
                            }
                        }
                        case "parent-group": {
                            for (val user : users) {
                                if (user.hasGroup(identifier)) usersFound.add(user.getPlayerName());
                            }
                            for (val group : groups) {
                                if (group.hasGroup(identifier)) groupsFound.add(group.getGroupId());
                            }
                        }
                    }
                    commandSource.sendMessage("&aFinding User's and Group's with " + args[2].toUpperCase() + "=" + identifier + " complete! (" + (System.currentTimeMillis() - start) + "ms.)");
                    val usersResult = usersFound.isEmpty() ? "&cNothing..." : Joiner.on(", ").join(usersFound);
                    val groupsResult = groupsFound.isEmpty() ? "&cNothing..." : Joiner.on(", ").join(groupsFound);
                    commandSource.sendMessage("&aResults for Users: &6" + usersResult);
                    commandSource.sendMessage("&aResults for Groups: &6" + groupsResult);
                }
            }
        });
    }

    public List<String> onTabComplete(Collection<String> onlinePlayers, String[] args) {
        switch (args.length) {
            case 0: return Collections.emptyList();
            case 1: return this.suggestMainCommand(args);
        }
        switch (args[0].toLowerCase()) {
            case "user": {
                return this.suggestUserCommand(onlinePlayers, this.manager.getStorage().getGroupNames(), args);
            }
            case "group": {
                return this.suggestGroupCommand(this.manager.getStorage().getGroupNames(), args);
            }
            case "find": {
                return this.suggestFindCommand(args);
            }
            case "export": {
                return this.suggestExportCommand(args);
            }
            case "migrate": {
                return this.suggestMigrateCommand(args);
            }
        }
        return Collections.emptyList();
    }

    private String getFinalArg(String[] args, int start) {
        return Joiner.on(" ").join(Arrays.copyOfRange(args, start, args.length));
    }

    private boolean checkSuperPermission(IMPCommandSource commandSource, String permission) {
        if (permission.contains(PermissionsMatcher.ROOT_WILDCARD) && !commandSource.hasPermission(PermissionsMatcher.ROOT_WILDCARD)) {
            commandSource.sendMessage("&cYou can't add root-permission because you don't have it!");
            return true;
        }
        return false;
    }

    private boolean checkAsteriskPermission(IMPCommandSource commandSource, Runnable runnable) {
        try {
            runnable.run();
        } catch (IllegalArgumentException exception) {
            commandSource.sendMessage("&c" + exception.getMessage());
            return true;
        }
        return false;
    }

    private void runOnLock(IMPCommandSource commandSource, Runnable runnable) {
        if (!this.highOperationsLock.writeLock().tryLock()) {
            commandSource.sendMessage("&cYou cannot run more than one heavy operation for security reasons, please wait.");
            return;
        }
        this.highOperationsLock.writeLock().lock();
        try {
            runnable.run();
        } finally {
            this.highOperationsLock.writeLock().unlock();
        }
    }

    private int parsePage(IMPCommandSource commandSource, String message) {
        int i;
        try {
            i = Integer.parseInt(message);
            if (i <= 0) {
                throw new Exception();
            }
        } catch (Exception ignored) {
            return 1;
        }
        return i;
    }

    private String spaced(int number, String symbol) {
        String integer = String.valueOf(number);
        StringBuilder builder = new StringBuilder();

        for (int a = 0; a < integer.length(); a++) {
            builder.append(integer.split("")[a]);

            if ((integer.length() - a + 2) % 3 != 0) {
                continue;
            }
            builder.append(symbol);
        }

        return builder.substring(0, builder.toString().length() - 1);
    }
}
