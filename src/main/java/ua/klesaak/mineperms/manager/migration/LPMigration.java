package ua.klesaak.mineperms.manager.migration;

import lombok.val;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import ua.klesaak.mineperms.manager.storage.Group;
import ua.klesaak.mineperms.manager.storage.User;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Юзать эту миграцию на свой страх и риск.
 * Так как MinePerms сделан на основе никнеймов игроков, и чувствителен к их регистру, когда LuckPerms хранит ники в .toLowerCase()
 * поэтому после миграции многие игроки обнаружат, что их привилегия теперь на никнейме shkolnik_top_killer_1337,
 * вместо ShKolNIk_ToP_KillEr_1337
 */

public class LPMigration implements IMigrationPlugin, AutoCloseable {
    private LuckPerms luckPerms;

    public LPMigration() {
        try {
            this.luckPerms = LuckPermsProvider.get();
        } catch (NoClassDefFoundError e) {
            throw new RuntimeException("LuckPerms plugin does not exists!");
        }
    }

    @Override
    public Collection<User> getAllUsers() {
        val userList = new ArrayList<User>();
        val userManager = this.luckPerms.getUserManager();
        for (UUID uuid : userManager.getUniqueUsers().join()) {
            val lpUser = userManager.loadUser(uuid).join();
            if (lpUser == null) continue;
            lpUser.getFriendlyName();
            if (lpUser.getFriendlyName().isEmpty()) continue;
            val mpUser = new User(lpUser.getFriendlyName(), lpUser.getPrimaryGroup());
            val permissions = new HashSet<String>();
            List<Node> nodes = new ArrayList<>(lpUser.getNodes());
            nodes.removeIf(NodeType.INHERITANCE.predicate(n -> n.getValue() && this.luckPerms.getGroupManager().isLoaded(n.getGroupName()))
                    .or(NodeType.META_OR_CHAT_META.predicate()));
            nodes.forEach(permissionNode -> permissions.add(permissionNode.getKey()));
            mpUser.setPermissions(permissions);
            userList.add(mpUser);
            if (userList.size() % 500 == 0) System.out.println("Migrated " + userList.size() + " users!");
        }
        return userList;
    }

    @Override
    public Collection<Group> getAllGroups() {
        val groupList = new ArrayList<Group>();
        val lpGroupManager = this.luckPerms.getGroupManager();
        lpGroupManager.loadAllGroups();

        for (val lpGroup : lpGroupManager.getLoadedGroups()) {
            val lpGroupCachedData = lpGroup.getCachedData();
            val inheritanceGroups = lpGroup.getNodes().stream()
                    .filter(NodeType.INHERITANCE::matches)
                    .map(NodeType.INHERITANCE::cast)
                    .map(InheritanceNode::getGroupName)
                    .collect(Collectors.toSet());

            val mpGroup = new Group(lpGroup.getName());
            mpGroup.setInheritanceGroups(inheritanceGroups);
            val permissions = new HashSet<String>();
            lpGroupCachedData.getPermissionData().getPermissionMap().forEach((permission, aBoolean) -> {
                if (aBoolean) permissions.add(permission);
            });
            mpGroup.setPermissions(permissions);
            mpGroup.setPrefix(lpGroupCachedData.getMetaData().getPrefix());
            mpGroup.setSuffix(lpGroupCachedData.getMetaData().getSuffix());
            groupList.add(mpGroup);
            if (groupList.size() % 10 == 0) System.out.println("Migrated " + groupList.size() + " groups!");
        }
        return groupList;
    }

    @Override
    public void close() throws Exception {
        this.luckPerms = null;
    }
}
