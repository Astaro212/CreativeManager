package com.astaro.creativemanager.commands.cm;

import com.astaro.creativemanager.CreativeManager;
import com.astaro.creativemanager.commands.Commands;

/**
 * Main command class.
 */
public class CreativeManagerCommands extends Commands {

    public CreativeManagerCommands(CreativeManager instance) {
        super(instance);
        registerCommands("reload", new ReloadSubCommands(getPlugin()));
        registerCommands("settings", new SettingsSubCommands(getPlugin()));
        registerCommands("infos", new InfosSubCommands(getPlugin()));
        registerCommands("items", new ItemsSubCommands(getPlugin()));
        setDefaultSubCmd("infos");
    }
}