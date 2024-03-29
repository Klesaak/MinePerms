package ua.klesaak.mineperms.manager.migration;

import lombok.val;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import ua.klesaak.mineperms.manager.storage.entity.Group;
import ua.klesaak.mineperms.manager.storage.entity.User;

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
            if (isUserEmpty(pexUser)) continue;
            String group = this.getGroupWithMaxWeight(pexUser.getParents()).toLowerCase();
            val mpUser = new User(pexUser.getName(), group);
            for (List<String> perms : pexUser.getAllPermissions().values()) {
                perms.forEach(mpUser::addOwnPermission);
            }
            mpUser.setPrefix(pexUser.getOwnPrefix());
            mpUser.setSuffix(pexUser.getOwnSuffix());
            userList.add(mpUser);
            if (userList.size() % 500 == 0) System.out.println("Migrated " + userList.size() + " users!");
        }
        return userList;
    }

    private boolean isUserEmpty(PermissionUser user) {
        if (user.getName().isEmpty() || user.getName() == null) return true;
        for (List<String> permissions : user.getAllPermissions().values()) {
            if (!permissions.isEmpty()) {
                return false;
            }
        }
        for (List<PermissionGroup> parents : user.getAllParents().values()) {
            if (!parents.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Collection<Group> getAllGroups() {
        val groupList = new ArrayList<Group>();
        for (val pexGroup : permissionManager.getGroupList()) {
            val mpGroup = new Group(pexGroup.getIdentifier());
            mpGroup.setInheritanceGroups(new HashSet<>(pexGroup.getOwnParentIdentifiers()));
            for (List<String> perms : pexGroup.getAllPermissions().values()) {
                perms.forEach(mpGroup::addOwnPermission);
            }
            mpGroup.setPrefix(pexGroup.getOwnPrefix());
            mpGroup.setSuffix(pexGroup.getOwnSuffix());
            groupList.add(mpGroup);
            if (groupList.size() % 10 == 0) System.out.println("Migrated " + groupList.size() + " groups!");
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
