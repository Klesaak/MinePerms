package ua.klesaak.mineperms.manager.migration;

import lombok.val;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import ua.klesaak.mineperms.manager.storage.Group;
import ua.klesaak.mineperms.manager.storage.User;

import java.util.*;
import java.util.stream.Collectors;

public class LPMigration implements IMigrationPlugin {
    private final LuckPerms luckPerms;

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
            if (lpUser.getUsername() == null || lpUser.getUsername().isEmpty()) continue;
            val lpUserCachedData = lpUser.getCachedData();
            val mpUser = new User(lpUser.getUsername(), lpUser.getPrimaryGroup());
            val permissions = new HashSet<String>();
            /////////////////////////////////////////////////////////////////////////////////////////////
            lpUserCachedData.getPermissionData().getPermissionMap().forEach((permission, aBoolean) -> {
                if (aBoolean) permissions.add(permission);                  // TODO: 06.06.2023 fix добавляются наследованные от групп права!!!
            });
            /////////////////////////////////////////////////////////////////////////////////////////////
            mpUser.setPermissions(permissions);
            mpUser.setPrefix(lpUserCachedData.getMetaData().getPrefix());
            mpUser.setSuffix(lpUserCachedData.getMetaData().getSuffix());
            userList.add(mpUser);
            if (userList.size() % 500 == 0) {
                //todo сообщение в лог
            }
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
            if (groupList.size() % 10 == 0) {
                //todo сообщение в лог
            }
        }
        return groupList;
    }
}
