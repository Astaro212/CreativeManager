package com.astaro.creativemanager.event.plugin;

import com.Acrobot.ChestShop.Events.PreShopCreationEvent;
import com.Acrobot.ChestShop.Events.PreTransactionEvent;
import com.astaro.creativemanager.CreativeManager;
import com.astaro.creativemanager.settings.Protections;
import com.astaro.creativemanager.utils.CMUtils;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;

public class ChestShop implements Listener {

    CreativeManager plugin;
    public ChestShop(CreativeManager plugin)
    {
        this.plugin = plugin;
    }


    @EventHandler
    public void onShopCreation(PreShopCreationEvent e)
    {
        if(!plugin.getSettings().getProtection(Protections.PL_CHESTSHOP)) return;
        if(!e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
        if(e.getPlayer().hasPermission("creativemanager.bypass.chestshop")) return;
        HashMap<String, String> replaceMap = new HashMap<>();
        replaceMap.put("{PLUGIN}", "ChestShop");
        CMUtils.sendMessage(e.getPlayer(), "permission.plugins", replaceMap);
        e.setCancelled(true);
    }
    @EventHandler
    public void onShopTransaction(PreTransactionEvent e)
    {
        if(!plugin.getSettings().getProtection(Protections.PL_CHESTSHOP)) return;
        if(!e.getClient().getGameMode().equals(GameMode.CREATIVE)) return;
        if(e.getClient().hasPermission("creativemanager.bypass.chestshop")) return;
        HashMap<String, String> replaceMap = new HashMap<>();
        replaceMap.put("{PLUGIN}", "ChestShop");
        CMUtils.sendMessage(e.getClient(), "permission.plugins", replaceMap);
        e.setCancelled(true);
    }
}
