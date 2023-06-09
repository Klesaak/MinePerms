package ua.klesaak.mineperms.manager.storage.mysql;

import com.google.gson.reflect.TypeToken;
import lombok.Setter;
import lombok.val;
import ua.klesaak.mineperms.manager.storage.entity.User;
import ua.klesaak.mineperms.manager.utils.JsonData;

import java.util.Set;

@Setter
public class UserData {
    private String playerName;
    private String group;
    private String prefix;
    private String suffix;
    private String serializedPerms;

    private UserData(String playerName, String group, String prefix, String suffix, Set<String> permissions) {
        this.playerName = playerName;
        this.group = group;
        this.prefix = prefix;
        this.suffix = suffix;
        this.serializedPerms = JsonData.GSON.toJson(permissions);
    }

    private UserData(User user) {
        this(user.getPlayerName(), user.getGroup(), user.getPrefix(), user.getSuffix(), user.getPermissions());
    }

    public static UserData from(User user) {
        return new UserData(user);
    }

    public User getUser() {
        val user = new User(this.playerName, this.group);
        user.setPrefix(this.prefix);
        user.setSuffix(this.suffix);
        user.setPermissions(this.getPermissions());
        return user;
    }

    protected UserData() {
    }

    public Set<String> getPermissions() {
        return JsonData.GSON.fromJson(this.serializedPerms, new TypeToken<Set<String>>(){}.getType());
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getGroup() {
        return group;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }
}
