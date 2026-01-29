package com.github.nautic.commands.customs;

import com.github.nautic.AtlasLang;
import com.github.nautic.manager.LanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AliasLangTabCompleter implements TabCompleter {

    private final LanguageManager languageManager;

    public AliasLangTabCompleter(AtlasLang plugin) {
        this.languageManager = plugin.getLanguageManager();
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {

        if (args.length == 1) {
            String input = args[0].toLowerCase();

            Set<String> locales = languageManager.getRegisteredLocales();

            return locales.stream()
                    .filter(locale -> locale.startsWith(input))
                    .sorted()
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}