package ua.klesaak.mineperms.manager.bulkupdate;

/**
 * Represents the data sets a query should apply to
 *
 * @author lucko
 *
 */
public enum DataType {
    ALL("all", true, true),
    USERS("users", true, false),
    GROUPS("groups", false, true);

    private final String name;
    private final boolean includingUsers;
    private final boolean includingGroups;

    DataType(String name, boolean includingUsers, boolean includingGroups) {
        this.name = name;
        this.includingUsers = includingUsers;
        this.includingGroups = includingGroups;
    }

    public String getName() {
        return this.name;
    }

    public boolean isIncludingUsers() {
        return this.includingUsers;
    }

    public boolean isIncludingGroups() {
        return this.includingGroups;
    }
}
