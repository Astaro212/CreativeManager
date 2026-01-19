package com.astaro.creativemanager.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public record BlockLog(String worldName, int x, int y, int z, UUID playerUUID){
    public BlockLog(Location loc, UUID playerUUID){
        this(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), playerUUID);
    }

    public Location toLocation(){
        World world = Bukkit.getWorld(worldName);
        return world != null ? new Location(world,x,y,z) : null;
    }
}
