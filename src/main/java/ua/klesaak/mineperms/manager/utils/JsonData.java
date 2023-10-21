package ua.klesaak.mineperms.manager.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;

import java.io.File;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

@Getter @Setter
public class JsonData {
    public static Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    private transient File file;

    @SneakyThrows
    public JsonData(File file) {
        if (!file.getParentFile().exists()) Files.createDirectory(file.getParentFile().toPath());
        if (!file.exists()) Files.createFile(file.toPath());
        this.file = file;
    }

    public JsonData() {
    }

    @SneakyThrows
    public <T> T readAll(TypeToken<? extends T> typeToken) {
        return GSON.fromJson(new String(Files.readAllBytes(this.file.toPath())), typeToken.getType());
    }

    @SneakyThrows
    public <T> T readAll(Class<? extends T> clazz) {
        return GSON.fromJson(new String(Files.readAllBytes(this.file.toPath())), clazz);
    }

    @SafeVarargs
    public static <T, V> Map<T, V> mapOf(Pair<T, V>... pairs) {
        val map = new HashMap<T, V>();
        for (Pair<T, V> pair : pairs) {
            map.putIfAbsent(pair.key, pair.value);
        }
        return map;
    }

    @SneakyThrows
    public static <T extends JsonData> T load(File file, Class<T> clazz) {
        if (file.exists() && file.length() != 0L) {
            T storage = GSON.fromJson(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8), clazz);
            storage.setFile(file);
            return storage;
        }
        if (!file.getParentFile().exists()) Files.createDirectory(file.getParentFile().toPath());
        if (!file.exists()) Files.createFile(file.toPath());
        T storage = clazz.newInstance();
        storage.setFile(file);
        storage.write(storage, true);
        return storage;
    }

    @SneakyThrows
    public void reload() {
        val clazz = this.getClass();
        val newClazz = load(this.file, clazz);
        for (val field : clazz.getDeclaredFields()) {
            val modifiers = field.getModifiers();
            if (Modifier.isTransient(modifiers)) continue;
            field.setAccessible(true);
            field.set(this, field.get(newClazz));
        }
    }

    public static <T, V> Pair<T, V> pairOf(T key, V value) {
        return new Pair<>(key, value);
    }

    @SneakyThrows
    public <T> void write(T source, boolean needTypeToken) {
        String json = needTypeToken ? GSON.toJson(source, new TypeToken<T>(){}.getType()) : GSON.toJson(source);
        Files.write(this.file.toPath(), json.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Getter
    public static class Pair<T, V> {
        private final T key;
        private final V value;

        public Pair(T key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}
