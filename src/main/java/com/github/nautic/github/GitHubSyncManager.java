package com.github.nautic.github;

import com.github.nautic.AtlasLang;
import com.github.nautic.manager.LanguageManager;

public class GitHubSyncManager {

    private final AtlasLang plugin;
    private final LanguageManager languageManager;

    public GitHubSyncManager(AtlasLang plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
    }

    public GitHubSyncResult sync() {

        try {
            GitHubConfig cfg = GitHubConfig.load(plugin.getConfig());

            GitHubZipSynchronizer synchronizer =
                    new GitHubZipSynchronizer(cfg, plugin.getDataFolder());

            GitHubSyncResult result = synchronizer.execute();

            if (result == GitHubSyncResult.SUCCESS && cfg.reloadAfterSync) {
                languageManager.reloadLanguages(plugin.getConfig());
            }

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return GitHubSyncResult.FAILED;
        }
    }
}
