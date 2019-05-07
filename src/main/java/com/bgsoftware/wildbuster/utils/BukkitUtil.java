package com.bgsoftware.wildbuster.utils;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import com.bgsoftware.wildbuster.WildBusterPlugin;
import com.bgsoftware.wildbuster.api.objects.BlockData;

import java.util.List;

public final class BukkitUtil {

    private static WildBusterPlugin plugin = WildBusterPlugin.getPlugin();

    public static void setFastBlock(Location loc, BlockData blockData){
        Bukkit.getScheduler().runTask(plugin, () -> plugin.getNMSAdapter().setFastBlock(loc, blockData));
    }

    public static void refreshChunks(World world, List<Chunk> chunksList){
        Bukkit.getScheduler().runTask(plugin, () -> plugin.getNMSAdapter().refreshChunks(world, chunksList));
    }

}