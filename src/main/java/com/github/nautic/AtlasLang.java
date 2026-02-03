package com.github.nautic;

import com.github.nautic.api.AtlasAPI;
import com.github.nautic.commands.ALCommands;
import com.github.nautic.commands.ALTabCompleter;
import com.github.nautic.commands.customs.LangsLoader;
import com.github.nautic.database.DatabaseManager;
import com.github.nautic.expansion.ALExpansion;
import com.github.nautic.expansion.AtlasLangExpansion;
import com.github.nautic.github.GitHubSyncManager;
import com.github.nautic.handler.LangHandler;
import com.github.nautic.libs.Libraries;
import com.github.nautic.manager.FileManager;
import com.github.nautic.manager.LanguageManager;
import com.github.nautic.update.spigot.SpigotUpdateListener;
import com.github.nautic.utils.addColor;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.zapper.DependencyManager;
import revxrsal.zapper.classloader.URLClassLoaderWrapper;
import revxrsal.zapper.relocation.Relocation;
import revxrsal.zapper.repository.Repository;

import java.io.File;
import java.net.URLClassLoader;

public final class AtlasLang extends JavaPlugin {

    private static AtlasLang instance;

    private FileManager fileManager;
    private LanguageManager languageManager;
    private LangHandler langHandler;
    private GitHubSyncManager gitHubSyncManager;

    @Override
    public void onLoad() {

        File libs = new File(getDataFolder(), "libraries");
        if (!libs.exists()) libs.mkdirs();

        getLogger().info("Installing runtime dependencies...");

        DependencyManager manager = new DependencyManager(
                libs,
                URLClassLoaderWrapper.wrap(
                        (URLClassLoader) getClassLoader()
                )
        );

        manager.repository(Repository.mavenCentral());
        manager.repository(Repository.maven("https://repo.codemc.org/repository/maven-public/"));

        for (Libraries lib : Libraries.values()) {
            manager.dependency(lib.dependency());
            for (Relocation relocation : lib.relocations()) {
                manager.relocate(relocation);
            }
        }

        manager.load();
    }

    @Override
    public void onEnable() {
        instance = this;

        new Metrics(this, 29130);
        new SpigotUpdateListener(this, 132278);

        saveDefaultConfig();

        File baseLangFolder = new File(getDataFolder(), "languages");
        if (!baseLangFolder.exists()) baseLangFolder.mkdirs();

        fileManager = new FileManager(baseLangFolder);
        languageManager = new LanguageManager(fileManager);
        langHandler = new LangHandler(fileManager, languageManager);

        languageManager.loadLanguagesFromConfig(getConfig());
        logRegisteredLanguages();

        DatabaseManager.loadDatabase();

        AtlasAPI.initialize(this);
        LangsLoader.registerLanguageCommands(this);

        gitHubSyncManager = new GitHubSyncManager(this);

        getCommand("atlaslang").setExecutor(new ALCommands(this));
        getCommand("atlaslang").setTabCompleter(new ALTabCompleter(this));

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ALExpansion(this).register();
            new AtlasLangExpansion(this).register();
            getLogger().info("PlaceholderAPI expansion registered.");
        } else {
            getLogger().warning("PlaceholderAPI not found.");
        }

        getLogger().info("AtlasLang enabled successfully.");
    }

    @Override
    public void onDisable() {
        DatabaseManager.close();
    }

    public static AtlasLang getInstance() {
        return instance;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public FileConfiguration getMainConfig() {
        return getConfig();
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public LangHandler getLangHandler() {
        return langHandler;
    }

    public GitHubSyncManager getGitHubSyncManager() {
        return gitHubSyncManager;
    }

    private void logRegisteredLanguages() {
        var map = languageManager.getLanguageMap();

        Bukkit.getConsoleSender().sendMessage(addColor.Set("&r"));
        Bukkit.getConsoleSender().sendMessage(
                addColor.Set("&#35ADFFAtlasLang &fLanguages Available &f(&a" + map.size() + "&f):")
        );

        for (var entry : map.entrySet()) {
            Bukkit.getConsoleSender().sendMessage(
                    addColor.Set("  &8- &f" + entry.getKey().toUpperCase() +
                            " &7(&b" + entry.getValue() + "&7)")
            );
        }

        Bukkit.getConsoleSender().sendMessage(addColor.Set("&r"));
    }
}
