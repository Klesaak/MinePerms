package ua.klesaak.mineperms.manager.migration;

import lombok.val;
import ru.Den_Abr.SimplePerms.Backends.Backend;
import ru.Den_Abr.SimplePerms.SimplePermsCommon;
import ua.klesaak.mineperms.manager.storage.Group;
import ua.klesaak.mineperms.manager.storage.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class SpermMigration implements IMigrationPlugin {
    private final Backend spermBackend;

    public SpermMigration() {
        try {
            this.spermBackend = SimplePermsCommon.getInstance().getPermissionManager().getBackend();
        } catch (NoClassDefFoundError e) {
            throw new RuntimeException("SimplePerm plugin by Den_Abr does not exists!");
        }
    }

    @Override
    public Collection<User> getAllUsers() {
        val userList = new ArrayList<User>();
        for (val spermUser : spermBackend.getAllUsers()) {
            val mpUser = new User(spermUser.getName(), spermUser.getUserGroup().getId());
            mpUser.setPermissions(new HashSet<>(spermUser.getPermissions()));
            mpUser.setPrefix(spermUser.getPrefix() == null ? "" : spermUser.getPrefix());
            mpUser.setSuffix(spermUser.getSuffix() == null ? "" : spermUser.getSuffix());
            userList.add(mpUser);
        }
        return userList;
    }

    @Override
    public Collection<Group> getAllGroups() {
        val groupList = new ArrayList<Group>();
        for (val spermGroup : spermBackend.getAllGroups()) {
            val mpGroup = new Group(spermGroup.getId());
            mpGroup.setInheritanceGroups(new HashSet<>(spermGroup.getAllParents()));
            mpGroup.setPermissions(new HashSet<>(spermGroup.getPermissions()));
            mpGroup.setPrefix(spermGroup.getPrefix() == null ? "" : spermGroup.getPrefix());
            mpGroup.setSuffix(spermGroup.getSuffix() == null ? "" : spermGroup.getSuffix());
            groupList.add(mpGroup);
        }
        return groupList;
    }
}
