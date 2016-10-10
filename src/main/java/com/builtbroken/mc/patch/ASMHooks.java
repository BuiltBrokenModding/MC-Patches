package com.builtbroken.mc.patch;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;

/**
 * Handles all injected ASM hook calls
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 10/9/2016.
 */
public final class ASMHooks
{
    /**
     * Recreated {@link TileEntityChest#checkForAdjacentChests()} while
     * ensuring no chunks are loaded in the process
     *
     * @param chest
     */
    public static void chestInvalidate(TileEntityChest chest)
    {
        try
        {
            if (!chest.adjacentChestChecked)
            {
                World world = chest.getWorldObj();
                chest.adjacentChestChecked = true;
                chest.adjacentChestZNeg = null;
                chest.adjacentChestXPos = null;
                chest.adjacentChestXNeg = null;
                chest.adjacentChestZPos = null;

                if (getBlock(chest, world, chest.xCoord - 1, chest.yCoord, chest.zCoord))
                {
                    chest.adjacentChestXNeg = (TileEntityChest) world.getTileEntity(chest.xCoord - 1, chest.yCoord, chest.zCoord);
                }

                if (getBlock(chest, world, chest.xCoord + 1, chest.yCoord, chest.zCoord))
                {
                    chest.adjacentChestXPos = (TileEntityChest) world.getTileEntity(chest.xCoord + 1, chest.yCoord, chest.zCoord);
                }

                if (getBlock(chest, world, chest.xCoord, chest.yCoord, chest.zCoord - 1))
                {
                    chest.adjacentChestZNeg = (TileEntityChest) world.getTileEntity(chest.xCoord, chest.yCoord, chest.zCoord - 1);
                }

                if (getBlock(chest, world, chest.xCoord, chest.yCoord, chest.zCoord + 1))
                {
                    chest.adjacentChestZPos = (TileEntityChest) world.getTileEntity(chest.xCoord, chest.yCoord, chest.zCoord + 1);
                }

                if (chest.adjacentChestZNeg != null)
                {
                    chest.adjacentChestZNeg.adjacentChestChecked = false;
                }

                if (chest.adjacentChestZPos != null)
                {
                    chest.adjacentChestZPos.adjacentChestChecked = false;
                }

                if (chest.adjacentChestXPos != null)
                {
                    chest.adjacentChestXPos.adjacentChestChecked = false;
                }

                if (chest.adjacentChestXNeg != null)
                {
                    chest.adjacentChestXNeg.adjacentChestChecked = false;
                }
            }
        }
        catch (Exception e)
        {
            CoreMod.logger.error("Failed to do chest invalidation logic...", e);
        }
    }

    private static boolean getBlock(TileEntityChest chest, World world, int x, int y, int z)
    {
        if (world == null)
        {
            return false;
        }
        else
        {
            if (world instanceof WorldServer)
            {
                ChunkProviderServer provider = ((WorldServer) world).theChunkProviderServer;
                if (!provider.chunkExists(x >> 4, z >> 4))
                {
                    return false;
                }
            }
            Block block = world.getBlock(x, y, z);
            return block instanceof BlockChest && ((BlockChest) block).field_149956_a == chest.func_145980_j();
        }
    }
}
