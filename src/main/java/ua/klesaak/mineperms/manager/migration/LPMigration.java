package ua.klesaak.mineperms.manager.migration;

import ua.klesaak.mineperms.manager.storage.Group;
import ua.klesaak.mineperms.manager.storage.User;

import java.util.Collection;

public class LPMigration implements IMigrationPlugin {

    public LPMigration() {

    }

    @Override
    public Collection<User> getAllUsers() {
        return null;
    }

    @Override
    public Collection<Group> getAllGroups() {
        return null;
    }
}
