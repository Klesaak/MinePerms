package ua.klesaak.mineperms.manager.storage.mysql;

import com.j256.ormlite.dao.Dao;
import lombok.Getter;
import ua.klesaak.mineperms.MinePermsManager;
import ua.klesaak.mineperms.manager.storage.Group;
import ua.klesaak.mineperms.manager.storage.Storage;
import ua.klesaak.mineperms.manager.storage.User;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Getter
public class MySQLStorage extends Storage {
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(3);
    private Dao<User, String> userDataDao;
    private Dao<Group, String> groupDataDao;

    public MySQLStorage(MinePermsManager manager) {
        super(manager);
    }

    @Override
    public void cacheUser(String nickName) {
        User user = this.temporalUsersCache.getIfPresent(nickName);
        if (user != null) {
            this.users.put(nickName, user);
            this.temporalUsersCache.invalidate(nickName);
        }
        //todo query
    }

    /**
     * При выходе игрока с сервера отгружаем его из основного кеша во временный
     * чтобы в случае быстрого перезахода игрока не тратить лишние ресурсы на его подгрузку из БД
     */
    @Override
    public void unCacheUser(String nickName) {
        User user = this.users.remove(nickName);
        this.temporalUsersCache.put(nickName, user);
    }

    @Override
    public void saveUser(String nickName) {

    }

    @Override
    public void saveUser(String nickName, User user) {

    }

    @Override
    public void saveGroup(String groupID) {

    }

    @Override
    public User getUser(String nickName) {
        return null;
    }

    @Override
    public String getUserPrefix(String nickName) {
        return null;
    }

    @Override
    public String getUserSuffix(String nickName) {
        return null;
    }

    @Override
    public void addUserPermission(String nickName, String permission) {

    }

    @Override
    public void removeUserPermission(String nickName, String permission) {

    }

    @Override
    public void setUserPrefix(String nickName, String prefix) {

    }

    @Override
    public void setUserSuffix(String nickName, String suffix) {

    }

    @Override
    public void setUserGroup(String nickName, String groupID) {

    }

    @Override
    public void deleteUser(String nickName) {

    }

    @Override
    public void updateUser(String nickName, User user) {

    }

    @Override
    public void addGroupPermission(String groupID, String permission) {

    }

    @Override
    public void removeGroupPermission(String groupID, String permission) {

    }

    @Override
    public void addGroupParent(String groupID, String parentID) {

    }

    @Override
    public void removeGroupParent(String groupID, String parentID) {

    }

    @Override
    public void setGroupPrefix(String groupID, String prefix) {

    }

    @Override
    public void setGroupSuffix(String groupID, String suffix) {

    }

    @Override
    public void deleteGroup(String groupID) {

    }

    @Override
    public void createGroup(String groupID) {

    }

    @Override
    public void updateGroup(String groupID, Group group) {

    }

    @Override
    public Collection<User> getAllUsersData() {
        return null;
    }

    @Override
    public Collection<Group> getAllGroupsData() {
        return null;
    }

    @Override
    public void close() {

    }
}
