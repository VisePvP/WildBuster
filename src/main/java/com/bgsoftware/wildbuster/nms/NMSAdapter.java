package com.bgsoftware.wildbuster.nms;

import com.bgsoftware.wildbuster.api.objects.BlockData;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface NMSAdapter {

    String getVersion();

    void setFastBlock(Location location, BlockData blockData);

    void refreshChunk(List<Player> playerList, Chunk chunk);

    void refreshLight(Chunk chunk);

    void clearTileEntities(Chunk chunk, List<Location> tileEntities);

    //void loadTileEntities(Collection<Location> tileEntities);

    void sendActionBar(Player player, String message);

    int getMaterialId(Material type);

    int getMaterialData(Block block);

    int getCombinedId(Block block);

    Object getBlockData(int combined);

    ItemStack getPlayerSkull(ItemStack itemStack, String texture);

    boolean isInsideBorder(Location location);

    Enchantment getGlowEnchant();
}
