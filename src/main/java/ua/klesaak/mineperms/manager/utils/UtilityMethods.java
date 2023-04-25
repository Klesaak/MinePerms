package ua.klesaak.mineperms.manager.utils;

import com.google.common.base.Preconditions;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class UtilityMethods {
    private static final String SERVER_PACKAGE_VERSION;

    static {
        Class<?> server = Bukkit.getServer().getClass();
        Matcher matcher = Pattern.compile("^org\\.bukkit\\.craftbukkit\\.(\\w+)\\.CraftServer$").matcher(server.getName());
        if (matcher.matches()) {
            SERVER_PACKAGE_VERSION = '.' + matcher.group(1) + '.';
        } else {
            SERVER_PACKAGE_VERSION = ".";
        }
    }

    public String obc(String className) {
        return "org.bukkit.craftbukkit" + SERVER_PACKAGE_VERSION + className;
    }

    public Class<?> obcClass(String className) throws ClassNotFoundException {
        return Class.forName(obc(className));
    }

    public <T extends Collection<? super String>> T copyPartialMatches(String token, Iterable<String> originals, T collection) {
        Preconditions.checkNotNull(token, "Search token cannot be null");
        Preconditions.checkNotNull(collection, "Collection cannot be null");
        Preconditions.checkNotNull(originals, "Originals cannot be null");
        originals.forEach(string -> { if (startsWithIgnoreCase(string, token)) collection.add(string);});
        return collection;
    }

    private boolean startsWithIgnoreCase(String string, String prefix) {
        Preconditions.checkNotNull(string, "Cannot check a null string for a match");
        return string.length() >= prefix.length() && string.regionMatches(true, 0, prefix, 0, prefix.length());
    }
}
