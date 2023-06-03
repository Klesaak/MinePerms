package ua.klesaak.mineperms.manager.migration;

import lombok.val;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import ua.klesaak.mineperms.manager.storage.Group;
import ua.klesaak.mineperms.manager.storage.User;

import java.util.ArrayList;
import java.util.Collection;

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
            //val mpUser = new User(pexUser.getName(), pexUser.getParents());
          //  pexUser.getName();
        }
        return userList;
    }

    @Override
    public Collection<Group> getAllGroups() {
        val groupList = new ArrayList<Group>();
        for (val pexGroup : permissionManager.getGroupList()) {

        }
        return groupList;
    }
}
