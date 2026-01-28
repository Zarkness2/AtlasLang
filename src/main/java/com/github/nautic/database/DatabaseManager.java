package com.github.nautic.database;

import com.github.nautic.AtlasLang;
import com.github.nautic.database.type.H2;
import com.github.nautic.database.type.MySQL;
import org.bukkit.configuration.ConfigurationSection;

public final class DatabaseManager {

    private static Database database;

    public static void loadDatabase() {
        AtlasLang plugin = AtlasLang.getInstance();
        String typeName = plugin.getMainConfig().getString("database.type", "H2");

        DatabaseType type;
        try {
            type = DatabaseType.valueOf(typeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid database.type, using H2.");
            type = DatabaseType.H2;
        }

        switch (type) {
            case MYSQL -> database = loadMySQL(plugin);
            case H2 -> database = new H2(plugin);
        }

        database.connect();
        database.load();
    }

    private static Database loadMySQL(AtlasLang plugin) {
        ConfigurationSection config = plugin.getMainConfig().getConfigurationSection("database");

        if (config == null) {
            plugin.getLogger().severe("Missing database section, falling back to H2.");
            return new H2(plugin);
        }

        return new MySQL(
                plugin,
                config.getString("address", "127.0.0.1"),
                config.getInt("port", 3306),
                config.getString("database", "AtlasLang"),
                config.getString("username", "root"),
                config.getString("password", "")
        );
    }

    public static Database getDatabase() {
        return database;
    }

    public static void close() {
        if (database != null) {
            database.close();
        }
    }
}
