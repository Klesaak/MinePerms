package ua.klesaak.mineperms.manager.utils;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.util.Collection;
import java.util.Map;

/**
 * Это класс обработки кешированного древа прав.
 * Использовать его только как обработчик а не как основной контейнер для хранения данных.
 *
 * @author sashok724 and Klesaak
 */
public final class PermissionsMatcher implements Cloneable {
    public static final String DASH_WILDCARD = "-";
    public static final String ROOT_WILDCARD = "*";
    public static final String DOT_WILDCARD = ".";
    public static final char DOT_CHAR_WILDCARD = '.';

    private boolean allowed;
    private final THashSet<String> exclusions = new THashSet<>(100);
    private final THashMap<String, PermissionsMatcher> exclusionsChild = new THashMap<>(100);


    /**
     * Юзаем этот метод в методах, где нужно перекрыть hasPermission
     * @return true or false
     */
    public boolean hasPermission(String permission) {
        int dotLocation = permission.indexOf(DOT_CHAR_WILDCARD);
        if (dotLocation == 0 || permission.endsWith(DOT_WILDCARD)) {
            return false;
        }
        if (dotLocation > 0) {
            String perm = permission.substring(0, dotLocation);
            PermissionsMatcher child = this.exclusionsChild.get(perm.toLowerCase());
            if (child == null) {
                return this.allowed;
            }
            perm = permission.substring(dotLocation + 1);
            return child.hasPermission(perm);
        }
        String permissionLC = permission.toLowerCase();
        return permissionLC.equals(ROOT_WILDCARD) ? this.allowed && this.exclusions.isEmpty() && this.exclusionsChild.isEmpty() : this.exclusions.contains(permissionLC) != this.allowed;
    }


    /**
     *
     * @param permission - право, которое добавляем(принимает так же антиправа по типу "-essentials.tpa")
     *                   если добавишь сюда звезду(*) - то все остальные пермишены очистятся и в кеше будет
     *                   только звезда.
     *
     * @param flag - является ли параметр permission обычным правом.
     *             Значение false - значит, что permission является анти-правом.
     */
    public void add(String permission, boolean flag) {
        int dotLocation = permission.indexOf(DOT_CHAR_WILDCARD);
        if (dotLocation == 0 || permission.endsWith(DOT_WILDCARD)) {
            throw new IllegalArgumentException("Permission can't start or end with dot: " + permission);
        }
        if (dotLocation > 0) {
            String perm = permission.substring(0, dotLocation);
            String beforeDot = perm.toLowerCase();
            if (beforeDot.contains(ROOT_WILDCARD)) {
                throw new IllegalArgumentException("Parent permission can't have asterisk: " + permission);
            }
            PermissionsMatcher child = this.exclusionsChild.get(beforeDot);
            if (flag == this.allowed && child == null) {
                return;
            }
            if (child == null) {
                child = new PermissionsMatcher();
                child.allowed = this.allowed;
                this.exclusionsChild.put(beforeDot, child);
            }
            child.add(permission.substring(dotLocation + 1), flag);
            if (child.allowed == this.allowed && child.exclusions.isEmpty() && child.exclusionsChild.isEmpty()) {
                this.exclusionsChild.remove(beforeDot);
            }
            return;
        }
        String permissionLC = permission.toLowerCase();
        if (permissionLC.equalsIgnoreCase(ROOT_WILDCARD)) {
            this.allowed = flag;
            this.exclusions.clear();
            this.exclusionsChild.clear();
        } else if (flag != this.allowed) {
            this.exclusions.add(permissionLC);
        } else {
            this.exclusions.remove(permissionLC);
        }
    }

    public void add(String permission) {
        this.add(permission.startsWith(DASH_WILDCARD) ? new StringBuilder(permission).deleteCharAt(0).toString() : permission, !permission.startsWith(DASH_WILDCARD));
    }

    public void add(Collection<String> permissions) {
        permissions.forEach(this::add);
    }

    public void clear() {
        this.allowed = false;
        this.exclusions.clear();
        this.exclusionsChild.clear();
    }

    public void dump(String prefix) {
        System.out.println(prefix + "allowed: " + this.allowed);
        System.out.println(prefix + "exclusions: " + this.exclusions);
        for (Map.Entry<String, PermissionsMatcher> entry : this.exclusionsChild.entrySet()) {
            String key = entry.getKey();
            PermissionsMatcher value = entry.getValue();
            System.out.println(prefix + key + ":");
            value.dump(prefix + "  ");
        }
    }

    @Override
    public PermissionsMatcher clone() throws CloneNotSupportedException {
        super.clone();
        PermissionsMatcher permissionsMatcher = new PermissionsMatcher();
        permissionsMatcher.allowed = this.allowed;
        permissionsMatcher.exclusions.addAll(this.exclusions);
        for (Map.Entry<String, PermissionsMatcher> entry : this.exclusionsChild.entrySet()) {
            String key = entry.getKey();
            PermissionsMatcher child = entry.getValue();
            permissionsMatcher.exclusionsChild.put(key, child.clone());
        }
        return permissionsMatcher;
    }
}
