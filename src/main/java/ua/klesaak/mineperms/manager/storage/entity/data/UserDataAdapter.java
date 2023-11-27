package ua.klesaak.mineperms.manager.storage.entity.data;

import com.google.gson.*;
import ua.klesaak.mineperms.manager.storage.entity.User;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public final class UserDataAdapter implements JsonDeserializer<User>, JsonSerializer<User> {

    @Override
    public User deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = (JsonObject)jsonElement;
        String userName = jsonObject.get("userName").getAsString();
        String prefix = jsonObject.get("prefix").getAsString();
        String suffix = jsonObject.get("suffix").getAsString();
        String group = jsonObject.get("group").getAsString();
        Set<String> permissions = new HashSet<>();
        jsonObject.get("permissions").getAsJsonArray().forEach(element -> permissions.add(element.getAsString()));
        User user = new User(userName, group);
        user.setPrefix(prefix);
        user.setSuffix(suffix);
        user.setPermissions(permissions);
        return user;
    }

    @Override
    public JsonElement serialize(User user, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userName", user.getPlayerName());
        jsonObject.addProperty("prefix", user.getPrefix());
        jsonObject.addProperty("suffix", user.getSuffix());
        jsonObject.addProperty("group", user.getGroup());
        JsonArray permissions = new JsonArray();
        user.getPermissions().forEach(permissions::add);
        jsonObject.add("permissions", permissions);
        return jsonObject;
    }
}
