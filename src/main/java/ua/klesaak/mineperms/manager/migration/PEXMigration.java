package ua.klesaak.mineperms.manager.migration;

import lombok.val;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import ua.klesaak.mineperms.manager.storage.Group;
import ua.klesaak.mineperms.manager.storage.User;

import java.util.*;

public class PEXMigration implements IMigrationPlugin {
    private final PermissionManager permissionManager;

    public PEXMigration() {
        try {
            this.permissionManager = PermissionsEx.getPermissionManager();
        } catch (NoClassDefFoundError e) {
            throw new RuntimeException("PermissionsEx plugin does not exists!");
        }
    }

    @Override
    public Collection<User> getAllUsers() {
        val userList = new ArrayList<User>();
        for (val pexUser : permissionManager.getUsers()) {
            String group = this.getGroupWithMaxWeight(pexUser.getParents()).toLowerCase();
            val mpUser = new User(pexUser.getName(), group);
            mpUser.setPermissions(new HashSet<>(pexUser.getOwnPermissions(null)));
            mpUser.setPrefix(pexUser.getPrefix() == null ? "" : pexUser.getPrefix());
            mpUser.setSuffix(pexUser.getSuffix() == null ? "" : pexUser.getSuffix());
            userList.add(mpUser);
        }
        return userList;
    }

    @Override
    public Collection<Group> getAllGroups() {
        val groupList = new ArrayList<Group>();
        for (val pexGroup : permissionManager.getGroupList()) {
            val mpGroup = new Group(pexGroup.getIdentifier());
            mpGroup.setInheritanceGroups(new HashSet<>(pexGroup.getOwnParentIdentifiers()));
            val permsSet = new HashSet<String>();
            for (List<String> perms : pexGroup.getAllPermissions().values()) {
                permsSet.addAll(perms);
            }
            mpGroup.setPermissions(permsSet);
            mpGroup.setPrefix(pexGroup.getPrefix() == null ? "" : pexGroup.getPrefix());
            mpGroup.setSuffix(pexGroup.getSuffix() == null ? "" : pexGroup.getSuffix());
            groupList.add(mpGroup);
        }
        return groupList;
    }

    private String getGroupWithMaxWeight(List<PermissionGroup> list) {
        if (list.isEmpty()) {
            return "default";
        }
        list = new ArrayList<>(list);
        Collections.sort(list);
        return list.get(0).getIdentifier();
    }
}
