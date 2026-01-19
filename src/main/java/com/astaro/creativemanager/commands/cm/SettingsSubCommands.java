package com.astaro.creativemanager.commands.cm;

import com.astaro.creativemanager.CreativeManager;
import com.astaro.creativemanager.commands.Commands;
import com.astaro.creativemanager.gui.settings.ProtectionSettingGui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SettingsSubCommands extends Commands {

    public SettingsSubCommands(CreativeManager instance) {
        super(instance, "creativemanager.admin", true);
    }

    @Override
    protected void run(CommandSender sender, String[] args) {
        new ProtectionSettingGui(plugin).open((Player) sender);
    }
}
