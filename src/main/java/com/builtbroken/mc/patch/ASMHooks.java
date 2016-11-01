package com.builtbroken.mc.patch;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;

import java.util.concurrent.Callable;

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
            //System.out.println("chestInvalidate(" + chest + ")");
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
            }

            if (chest.adjacentChestZNeg != null)
            {
                chest.adjacentChestZNeg.adjacentChestChecked = false;
                chest.adjacentChestZPos = null;
            }

            if (chest.adjacentChestZPos != null)
            {
                chest.adjacentChestZPos.adjacentChestChecked = false;
                chest.adjacentChestZNeg = null;
            }

            if (chest.adjacentChestXPos != null)
            {
                chest.adjacentChestXPos.adjacentChestChecked = false;
                chest.adjacentChestXNeg = null;
            }

            if (chest.adjacentChestXNeg != null)
            {
                chest.adjacentChestXNeg.adjacentChestChecked = false;
                chest.adjacentChestXPos = null;
            }
            //System.out.println("\t done (" + chest + ")");
        }
        catch (Exception e)
        {
            CoreMod.logger.error("Failed to do chest invalidation logic...", e);
        }
    }

    /**
     * Handles method calls for {@link World#notifyBlockOfNeighborChange(int, int, int, Block)}
     * in order to prevent chunk loading.
     */
    public static void notifyBlockOfNeighborChange(World world, int x, int y, int z, Block block1)
    {
        //System.out.println("notifyBlockOfNeighborChange(" + world + ", " + x + ", " + y + ", " + z + ", " + block1 + ")");
        Block block = getBlock(world, x, y, z);
        if (block != null)
        {
            try
            {
                block.onNeighborBlockChange(world, x, y, z, block1);
            }
            catch (Throwable throwable1)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable1, "Exception while updating neighbours");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being updated");
                int l;

                try
                {
                    l = world.getBlockMetadata(x, y, z);
                }
                catch (Throwable throwable)
                {
                    l = -1;
                }

                crashreportcategory.addCrashSectionCallable("Source block type", new Callable()
                {
                    private static final String __OBFID = "CL_00000142";

                    public String call()
                    {
                        try
                        {
                            return String.format("ID #%d (%s // %s)", new Object[]{Integer.valueOf(Block.getIdFromBlock(block1)), block1.getUnlocalizedName(), block1.getClass().getCanonicalName()});
                        }
                        catch (Throwable throwable2)
                        {
                            return "ID #" + Block.getIdFromBlock(block1);
                        }
                    }
                });
                CrashReportCategory.func_147153_a(crashreportcategory, x, y, z, block, l);
                throw new ReportedException(crashreport);
            }
        }
        //System.out.println("\t done (" + world + ", " + x + ", " + y + ", " + z + ", " + block1 + ")");
    }

    private static boolean getBlock(TileEntityChest chest, World world, int x, int y, int z)
    {
        Block block = getBlock(world, x, y, z);
        return block instanceof BlockChest && ((BlockChest) block).field_149956_a == chest.func_145980_j();
    }

    private static Block getBlock(World world, int x, int y, int z)
    {
        if (world == null)
        {
            return null;
        }
        else
        {
            if (world instanceof WorldServer)
            {
                ChunkProviderServer provider = ((WorldServer) world).theChunkProviderServer;
                if (!provider.chunkExists(x >> 4, z >> 4))
                {
                    return null;
                }
            }
            return world.getBlock(x, y, z);
        }
    }
}
