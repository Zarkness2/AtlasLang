package com.github.nautic.manager;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LanguageManager {

    private final FileManager fileManager;
    private final Map<String, String> languageMap = new HashMap<>();
    private String defaultLang = "english";

    public LanguageManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public void loadLanguagesFromConfig(FileConfiguration config) {
        languageMap.clear();

        defaultLang = config.getString("default", "english").toLowerCase();

        for (String entry : config.getStringList("register.languages")) {
            String[] parts = entry.split(":");
            if (parts.length != 3) continue;

            String locale = parts[0].toLowerCase();
            String folder = parts[1].toLowerCase();
            String defaultFile = parts[2];

            languageMap.put(locale, folder);

            fileManager.prepareLanguage(folder, defaultFile);
            fileManager.loadLanguageFolder(folder);
        }
    }

    public String resolveLanguageStrict(String input) {
        if (input == null || input.isEmpty()) return null;

        input = input.toLowerCase();

        if (languageMap.containsKey(input)) {
            return languageMap.get(input);
        }

        if (languageMap.containsValue(input)) {
            return input;
        }

        return null;
    }

    public boolean isRegisteredLanguage(String lang) {
        return lang != null && languageMap.containsValue(lang.toLowerCase());
    }

    public Set<String> getRegisteredLanguages() {
        return new HashSet<>(languageMap.values());
    }

    public String getDefaultLang() {
        return defaultLang;
    }

    public void reloadLanguages(FileConfiguration config) {
        languageMap.clear();
        fileManager.clearCache();
        loadLanguagesFromConfig(config);
    }

    public Map<String, String> getLanguageMap() {
        return new HashMap<>(languageMap);
    }

    public Set<String> getRegisteredLocales() {
        return new HashSet<>(languageMap.keySet());
    }
}