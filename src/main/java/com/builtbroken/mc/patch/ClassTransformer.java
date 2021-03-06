package com.builtbroken.mc.patch;

import net.minecraft.block.Block;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

import static org.objectweb.asm.Opcodes.*;

/**
 * Handles transformation of several MC classes. See each method for information about what is edited.
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 10/9/2016.
 */
public final class ClassTransformer implements IClassTransformer
{
    /** {@link net.minecraft.tileentity.TileEntityChest} */
    private static final String CLASS_KEY_TILE_ENTITY = "net.minecraft.tileentity.TileEntityChest";
    /** {@link net.minecraft.world.World} */
    private static final String CLASS_KEY_WORLD = "net.minecraft.world.World";
    /** {@link ASMHooks} */
    private static final String HOOK_CLASS = "com/builtbroken/mc/patch/ASMHooks";


    @Override
    public byte[] transform(String clazz, String name, byte[] bytes)
    {
        try
        {
            if (name.equals(CLASS_KEY_TILE_ENTITY))
            {
                ClassNode cn = ASMUtility.startInjection(name, bytes);
                injectInvalidateEdit(cn);
                return ASMUtility.finishInjection(name, cn);
            }
            else if (name.equals(CLASS_KEY_WORLD))
            {
                ClassNode cn = ASMUtility.startInjection(name, bytes);
                injectNotifyBlockOfNeighborChange(cn);
                return ASMUtility.finishInjection(name, cn);
            }
        }
        catch (Exception e)
        {
            final String msg = "Unexpected error while patching '" + name +"' skipping patches for class. Expect issues....";
            if(CoreMod.isDevMode())
            {
                throw new RuntimeException(msg, e);
            }
            else
            {
                CoreMod.logger.error(msg, e);
            }
        }
        return bytes;
    }

    /** Fixes {@link net.minecraft.tileentity.TileEntityChest#invalidate()} causing inf loops on chunk edges */
    private void injectInvalidateEdit(ClassNode cn)
    {
        final MethodNode method = ASMUtility.getMethod(cn, "invalidate", "func_145843_s");

        if (method != null)
        {
            //Create method call
            final InsnList nodeAdd = new InsnList();
            nodeAdd.add(new VarInsnNode(ALOAD, 0));
            nodeAdd.add(new MethodInsnNode(INVOKESTATIC, HOOK_CLASS, "chestInvalidate", "(Lnet/minecraft/tileentity/TileEntityChest;)V", false));

            //Inject method call at top of method
            ListIterator<AbstractInsnNode> it = method.instructions.iterator();
            MethodInsnNode checkForAdjacentChests = null;
            while (it.hasNext())
            {
                AbstractInsnNode node = it.next();
                if (node instanceof MethodInsnNode)
                {
                    if (((MethodInsnNode) node).name.equals("checkForAdjacentChests"))
                    {
                        checkForAdjacentChests = (MethodInsnNode) node;
                    }
                }
            }
            if (checkForAdjacentChests != null)
            {
                //Inject replacement
                method.instructions.insertBefore(method.instructions.get(method.instructions.size() - 1), nodeAdd);
                //Remove broken code
                method.instructions.remove(checkForAdjacentChests);
            }
        }
        else
        {
            CoreMod.logger.error("Failed to find 'public void invalidate()' in TileEntityChest.class");
        }
    }

    /** Fixes {@link net.minecraft.world.World#notifyBlockOfNeighborChange(int, int, int, Block)} causing chunks to load */
    private void injectNotifyBlockOfNeighborChange(ClassNode cn)
    {
        final MethodNode method = ASMUtility.getMethod(cn, "notifyBlockOfNeighborChange", "func_147460_e");

        if (method != null)
        {
            final InsnList edit = new InsnList();
            edit.add(new VarInsnNode(ALOAD, 0));
            edit.add(new VarInsnNode(ILOAD, 1)); //TODO update as needed
            edit.add(new VarInsnNode(ILOAD, 2));
            edit.add(new VarInsnNode(ILOAD, 3));
            edit.add(new VarInsnNode(ALOAD, 4));
            edit.add(new MethodInsnNode(INVOKESTATIC, HOOK_CLASS, "notifyBlockOfNeighborChange", "(Lnet/minecraft/world/World;IIILnet/minecraft/block/Block;)V", false));
            edit.add(new InsnNode(RETURN));
            MethodInsnNode m = ASMUtility.getMethodeNode(method, "onNeighborBlockChange", "func_149695_a");
            method.instructions.insertBefore(m, edit);
            method.instructions.remove(m);
        }
        else
        {
            CoreMod.logger.error("Failed to find 'public void notifyBlockOfNeighborChange(int, int, int, Block)' in World.class");
        }
    }
}
