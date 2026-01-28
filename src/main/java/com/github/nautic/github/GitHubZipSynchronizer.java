package com.github.nautic.github;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GitHubZipSynchronizer {

    private final GitHubConfig cfg;
    private final File pluginFolder;

    private final Set<String> remoteFiles = new HashSet<>();

    public GitHubZipSynchronizer(GitHubConfig cfg, File pluginFolder) {
        this.cfg = cfg;
        this.pluginFolder = pluginFolder;
    }

    public GitHubSyncResult execute() throws IOException {

        File zipFile = downloadRepositoryZip();
        if (zipFile == null) return GitHubSyncResult.FAILED;

        File localRoot = new File(pluginFolder, cfg.localRoot);
        if (!localRoot.exists()) localRoot.mkdirs();

        unzipAndSync(zipFile, localRoot);

        if (cfg.deleteMissing) {
            deleteMissingLocalFiles(localRoot);
        }

        zipFile.delete();
        return GitHubSyncResult.SUCCESS;
    }

    private File downloadRepositoryZip() throws IOException {

        String url = "https://github.com/"
                + cfg.repository
                + "/archive/refs/heads/"
                + cfg.branch
                + ".zip";

        HttpURLConnection conn =
                (HttpURLConnection) new URL(url).openConnection();

        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);

        if (cfg.authType == GitHubConfig.AuthType.TOKEN && !cfg.token.isEmpty()) {
            conn.setRequestProperty("Authorization", "token " + cfg.token);
        }

        if (conn.getResponseCode() != 200) {
            throw new IOException("GitHub HTTP " + conn.getResponseCode());
        }

        File tempZip = new File(pluginFolder, "github_sync.zip");

        try (InputStream in = conn.getInputStream()) {
            Files.copy(in, tempZip.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        return tempZip;
    }

    private void unzipAndSync(File zip, File localRoot) throws IOException {

        try (ZipInputStream zis =
                     new ZipInputStream(new FileInputStream(zip))) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {

                String name = entry.getName();

                int firstSlash = name.indexOf('/');
                if (firstSlash == -1) continue;
                name = name.substring(firstSlash + 1);

                if (!name.startsWith(cfg.remoteRoot + "/")) continue;

                String relativePath =
                        name.substring(cfg.remoteRoot.length() + 1);

                if (relativePath.isEmpty()) continue;

                File target = new File(localRoot, relativePath);
                remoteFiles.add(relativePath.replace("\\", "/"));

                if (entry.isDirectory()) {
                    target.mkdirs();
                    continue;
                }

                target.getParentFile().mkdirs();

                if (target.exists() && !cfg.overwriteExisting) {
                    continue;
                }

                try (OutputStream out = new FileOutputStream(target)) {
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                }
            }
        }
    }

    private void deleteMissingLocalFiles(File root) {

        walkAndDelete(root, "");
    }

    private void walkAndDelete(File file, String relative) {

        if (file.isDirectory()) {
            for (File child : Objects.requireNonNull(file.listFiles())) {
                walkAndDelete(child,
                        relative.isEmpty()
                                ? child.getName()
                                : relative + "/" + child.getName());
            }
            return;
        }

        if (!remoteFiles.contains(relative.replace("\\", "/"))) {
            file.delete();
        }
    }
}
