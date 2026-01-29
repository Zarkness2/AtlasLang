package com.github.nautic.handler;

import com.github.nautic.manager.FileManager;
import com.github.nautic.manager.LanguageManager;
import com.github.nautic.utils.addColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class LangHandler {

    private final FileManager fileManager;
    private final LanguageManager languageManager;

    public LangHandler(FileManager fileManager, LanguageManager languageManager) {
        this.fileManager = fileManager;
        this.languageManager = languageManager;
    }

    public String get(String langInput, String filePath, String path) {
        return get(null, langInput, filePath, path);
    }

    public String get(Player player, String langInput, String filePath, String path) {

        String langFolder = languageManager.resolveLanguageStrict(langInput);
        if (langFolder == null) {
            langFolder = languageManager.getDefaultLang();
        }

        String fileId = langFolder + ":" + filePath.toLowerCase();

        if (!fileManager.isLoaded(fileId)) {
            fileManager.loadByLangAndPath(langFolder, filePath + ".yml");
        }

        if (!fileManager.isLoaded(fileId)) {
            return addColor.SetPlaceholders(player,
                    getSystemMessageOrDefault(
                            langFolder,
                            "file_not_found",
                            "&cFile not found &7[" + filePath + "]"
                    ).replace("{file}", filePath)
            );
        }

        FileConfiguration cfg = fileManager.getConfig(fileId);
        if (cfg == null) {
            return addColor.SetPlaceholders(player,
                    getSystemMessage(langFolder, "invalid_lang_format")
            );
        }

        String result = null;

        if (cfg.isString(path)) {
            result = cfg.getString(path);
        }

        else if (cfg.isList(path)) {
            List<String> list = cfg.getStringList(path);
            if (!list.isEmpty()) {
                result = String.join("\n", list);
            }
        }

        if (result == null) {
            result = getSystemMessageOrDefault(
                    langFolder,
                    "not_translated",
                    "&fNot translated &7» &a" + path
            ).replace("{path}", path);
        }

        return addColor.SetPlaceholders(player, result.trim());
    }

    public String getSystemMessage(String langFolder, String key) {
        String systemId = langFolder.toLowerCase() + ":" + langFolder.toLowerCase();

        if (!fileManager.isLoaded(systemId)) {
            return addColor.Set("&cSystem file missing");
        }

        String msg = fileManager.get(systemId, key);
        if (msg == null) {
            return addColor.Set("&cSystem message missing");
        }

        return addColor.Set(msg.trim());
    }

    private String getSystemMessageOrDefault(String langFolder, String key, String defaultMsg) {
        String systemId = langFolder.toLowerCase() + ":" + langFolder.toLowerCase();

        if (!fileManager.isLoaded(systemId)) {
            return addColor.Set(defaultMsg);
        }

        String msg = fileManager.get(systemId, key);
        if (msg == null) {
            return addColor.Set(defaultMsg);
        }

        return addColor.Set(msg.trim());
    }
}