package com.astaro.creativemanager.commands.cm;

import com.astaro.creativemanager.CreativeManager;
import com.astaro.creativemanager.commands.Commands;
import fr.k0bus.k0buscore.utils.StringUtils;
import org.bukkit.command.CommandSender;

public class ReloadSubCommands extends Commands {

    public ReloadSubCommands(CreativeManager instance) {
        super(instance, "creativemanager.admin", false);
    }

    @Override
    protected void run(CommandSender sender, String[] args) {
        plugin.loadConfigManager();
        sender.sendMessage(CreativeManager.TAG + StringUtils.translateColor("&5Configuration reloaded !"));
    }
}
