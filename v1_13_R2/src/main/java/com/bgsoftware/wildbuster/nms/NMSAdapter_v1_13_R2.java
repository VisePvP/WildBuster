package com.bgsoftware.wildbuster.nms;

import com.bgsoftware.wildbuster.api.objects.BlockData;
import net.minecraft.server.v1_13_R2.Block;
import net.minecraft.server.v1_13_R2.BlockPosition;
import net.minecraft.server.v1_13_R2.ChatMessage;
import net.minecraft.server.v1_13_R2.ChatMessageType;
import net.minecraft.server.v1_13_R2.Chunk;
import net.minecraft.server.v1_13_R2.ChunkSection;
import net.minecraft.server.v1_13_R2.IBlockData;
import net.minecraft.server.v1_13_R2.IChatBaseComponent;
import net.minecraft.server.v1_13_R2.ItemStack;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import net.minecraft.server.v1_13_R2.NBTTagList;
import net.minecraft.server.v1_13_R2.PacketPlayOutChat;
import net.minecraft.server.v1_13_R2.PacketPlayOutMultiBlockChange;
import net.minecraft.server.v1_13_R2.TileEntityHopper;
import net.minecraft.server.v1_13_R2.World;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.WorldBorder;
import org.bukkit.craftbukkit.v1_13_R2.CraftChunk;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_13_R2.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
public final class NMSAdapter_v1_13_R2 implements NMSAdapter {

    @Override
    public String getVersion() {
        return "v1_13_R2";
    }

    @Override
    public void setFastBlock(Location location, BlockData blockData) {
        Chunk chunk = ((CraftWorld) location.getWorld()).getHandle().getChunkAt(location.getBlockX() >> 4, location.getBlockZ() >> 4);
        int indexY = location.getBlockY() >> 4;
        ChunkSection chunkSection = chunk.getSections()[indexY];

        if(chunkSection == null)
            chunkSection = chunk.getSections()[indexY] = new ChunkSection(indexY << 4, chunk.world.worldProvider.g());

        chunkSection.setType(location.getBlockX() & 15, location.getBlockY() & 15, location.getBlockZ() & 15, Block.getByCombinedId(blockData.getCombinedId()));
    }

    @Override
    public void refreshChunk(org.bukkit.Chunk bukkitChunk, List<Location> blocksList, List<Player> playerList) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        int blocksAmount = blocksList.size();
        short[] values = new short[blocksAmount];

        Location firstLocation = null;

        int counter = 0;
        for(Location location : blocksList) {
            if(firstLocation == null)
                firstLocation = location;

            values[counter++] = (short) ((location.getBlockX() & 15) << 12 | (location.getBlockZ() & 15) << 8 | location.getBlockY());
        }

        PacketPlayOutMultiBlockChange multiBlockChange = new PacketPlayOutMultiBlockChange(blocksAmount, values, chunk);

        for(Player player : playerList)
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(multiBlockChange);
    }

    @Override
    public void refreshLight(org.bukkit.Chunk chunk) {
        ((CraftChunk) chunk).getHandle().initLighting();
    }

    @Override
    public void clearTileEntities(org.bukkit.Chunk bukkitChunk, List<Location> tileEntities) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        new HashMap<>(chunk.tileEntities).forEach(((blockPosition, tileEntity) -> {
            Location location = new Location(bukkitChunk.getWorld(), blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
            if(tileEntities.contains(location))
                chunk.tileEntities.remove(blockPosition);
        }));
    }

    @Override
    public void sendActionBar(Player pl, String message) {
        IChatBaseComponent chatBaseComponent = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + message + "\"}");
        PacketPlayOutChat packet = new PacketPlayOutChat(chatBaseComponent, ChatMessageType.GAME_INFO);
        ((CraftPlayer) pl).getHandle().playerConnection.sendPacket(packet);
    }

    @Override
    public int getMaterialId(Material type) {
        throw new RuntimeException("You cannot run getMaterialId in 1.13.X!");
    }

    @Override
    public int getMaterialData(org.bukkit.block.Block block) {
        World world = ((CraftWorld) block.getWorld()).getHandle();
        IBlockData blockData = world.getType(new BlockPosition(block.getX(), block.getY(), block.getZ()));
        return CraftMagicNumbers.toLegacyData(blockData);
    }

    @Override
    public int getCombinedId(org.bukkit.block.Block block) {
        World world = ((CraftWorld) block.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        return Block.getCombinedId(world.getType(blockPosition));
    }

    @Override
    public Object getBlockData(int combined) {
        return CraftBlockData.fromData(Block.getByCombinedId(combined));
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public org.bukkit.inventory.ItemStack getPlayerSkull(org.bukkit.inventory.ItemStack itemStack, String texture) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);

        NBTTagCompound nbtTagCompound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();

        NBTTagCompound skullOwner = nbtTagCompound.hasKey("SkullOwner") ? nbtTagCompound.getCompound("SkullOwner") : new NBTTagCompound();

        NBTTagCompound properties = new NBTTagCompound();

        NBTTagList textures = new NBTTagList();
        NBTTagCompound signature = new NBTTagCompound();
        signature.setString("Value", texture);
        textures.add(signature);

        properties.set("textures", textures);

        skullOwner.set("Properties", properties);
        skullOwner.setString("Id", UUID.randomUUID().toString());

        nbtTagCompound.set("SkullOwner", skullOwner);

        nmsItem.setTag(nbtTagCompound);

        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override
    public boolean isInsideBorder(Location location) {
        WorldBorder worldBorder = location.getWorld().getWorldBorder();
        Location center = worldBorder.getCenter();
        int radius = (int) worldBorder.getSize() / 2;
        return location.getBlockX() <=(center.getBlockX() + radius) && location.getBlockX() >= (center.getBlockX() - radius) &&
                location.getBlockZ() <= (center.getBlockZ() + radius) && location.getBlockZ() >= (center.getBlockZ() - radius);
    }

    @Override
    public Enchantment getGlowEnchant() {
        return new Enchantment(NamespacedKey.minecraft("wb_glowing_enchant")) {
            @Override
            public String getName() {
                return "WildBusterGlow";
            }

            @Override
            public int getMaxLevel() {
                return 1;
            }

            @Override
            public int getStartLevel() {
                return 0;
            }

            @Override
            public EnchantmentTarget getItemTarget() {
                return null;
            }

            @Override
            public boolean conflictsWith(Enchantment enchantment) {
                return false;
            }

            @Override
            public boolean canEnchantItem(org.bukkit.inventory.ItemStack itemStack) {
                return true;
            }

            @Override
            public boolean isTreasure() {
                return false;
            }

            @Override
            public boolean isCursed() {
                return false;
            }
        };
    }

    @Override
    public boolean isTallGrass(Material type) {
        switch (type){
            case SUNFLOWER:
            case LILAC:
            case TALL_GRASS:
            case LARGE_FERN:
            case ROSE_BUSH:
            case PEONY:
            case TALL_SEAGRASS:
                return true;
            default:
                return false;
        }
    }

    @Override
    public Object getCustomHolder(InventoryType inventoryType, InventoryHolder defaultHolder, String title) {
        return new CustomTileEntityHopper(defaultHolder, title);
    }

    private static class CustomTileEntityHopper extends TileEntityHopper {

        private final InventoryHolder holder;

        CustomTileEntityHopper(InventoryHolder holder, String title){
            this.holder = holder;
            this.setCustomName(new ChatMessage(title));
        }

        @Override
        public InventoryHolder getOwner() {
            return holder;
        }
    }

}
