package ua.klesaak.mineperms.manager.storage.sql;

import com.zaxxer.hikari.HikariDataSource;
import lombok.val;
import ua.klesaak.mineperms.manager.log.MPLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public interface SQLLoader {
    Pattern SPACE_PATTERN = Pattern.compile("\\s{2,}"); //фиксим большие пробелы
    
    default String loadSQL(String name, Object... placeholders) {
        String sqlFile = "sql/" + name + ".sql";
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(sqlFile)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Could not find " + sqlFile);
            }
            try (val bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String sqlLine = bufferedReader.lines().filter(line -> !line.trim().isEmpty() && !line.trim().startsWith("--")).collect(Collectors.joining(" "));
                if (placeholders.length != 0 && placeholders.length % 2 == 0) {
                    int i = 0;
                    for (Object placeholder : placeholders) {
                        if (i % 2 == 0) {
                            sqlLine = sqlLine.replace(String.valueOf(placeholder), String.valueOf(placeholders[i + 1]));
                        }
                        ++i;
                    }
                }
                Matcher matcher = SPACE_PATTERN.matcher(sqlLine);
                if (matcher.find()) return matcher.replaceAll(Matcher.quoteReplacement(" "));
                return sqlLine;
            } catch (IOException e) {
                MPLogger.logError(new RuntimeException("Error while load SQL file!"));
            }
        } catch (IOException e) {
            MPLogger.logError(new RuntimeException("Error while load SQL file!"));
        }
        return "";
    }

    default void executeSQL(HikariDataSource hikariDataSource, String sql) {
        Collection<String> sqlList = new ArrayList<>(16);
        if (!sql.contains(";")) throw new IllegalArgumentException("Missed ';' in sql line: '" + sql + "'");
        sqlList.addAll(Arrays.asList(sql.split(";")));
        sqlList.forEach(sqlLine -> {
            try (val con = hikariDataSource.getConnection(); val statement = con.prepareStatement(sqlLine)) {
                statement.execute();
            } catch (SQLException e) {
                MPLogger.logError(new RuntimeException("Error while executeSQL data", e));
            }
        });
    }

    default void executeQuery(Callback callback, HikariDataSource hikariDataSource, String request, Object... args) {
        try (val con = hikariDataSource.getConnection(); val statement = con.prepareStatement(request)) {
            for(int i = 0; i < args.length; ++i) {
                statement.setObject(i + 1, args[i]);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                callback.call(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while load data from sql ", e);
        }
    }

    default void executeUpdate(HikariDataSource hikariDataSource, String request, Object... args) {
        try (val con = hikariDataSource.getConnection(); val statement = con.prepareStatement(request)) {
            for(int i = 0; i < args.length; ++i) {
                statement.setObject(i + 1, args[i]);
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error while update data ", e);
        }
    }

    interface Callback {
        void call(ResultSet resultSet) throws SQLException;
    }
}
