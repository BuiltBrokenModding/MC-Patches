package com.builtbroken.mc.patch;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.tileentity.TileEntity;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 10/9/2016.
 */
public class ClassTransformer implements IClassTransformer
{
    private static final String CLASS_KEY_TILE_ENTITY = "net.minecraft.tileentity.TileEntityChest";
    private static final String HOOK_CLASS = "com/builtbroken/mc/patch/ASMHooks";


    @Override
    public byte[] transform(String name, String transformerName, byte[] bytes)
    {
        if (name.equals(CLASS_KEY_TILE_ENTITY))
        {
            ClassNode cn = startInjection(bytes);
            injectInvalidateEdit(cn);
            return finishInjection(cn);
        }
        return bytes;
    }

    /** Fixed {@link TileEntity#invalidate()} causing inf loops on chunk edges */
    private void injectInvalidateEdit(ClassNode cn)
    {
        final MethodNode method = getMethod(cn, "invalidate", "()V");

        if (method != null)
        {
            //Create method call
            final InsnList nodeAdd = new InsnList();
            nodeAdd.add(new VarInsnNode(Opcodes.ALOAD, 0));
            nodeAdd.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK_CLASS, "chestInvalidate", "(Lnet/minecraft/tileentity/TileEntityChest;)V", false));

            //Inject method call at top of method
            ListIterator<AbstractInsnNode> it = method.instructions.iterator();
            MethodInsnNode checkForAdjacentChests = null;
            while(it.hasNext())
            {
                AbstractInsnNode node = it.next();
                if(node instanceof MethodInsnNode)
                {
                    if(((MethodInsnNode) node).name.equals("checkForAdjacentChests"))
                    {
                        checkForAdjacentChests = (MethodInsnNode) node;
                    }
                }
            }
            if(checkForAdjacentChests != null)
            {
                //Inject replacement
                method.instructions.add(nodeAdd);
                //Remove broken code
                method.instructions.remove(checkForAdjacentChests);
            }
        }
    }

    private ClassNode startInjection(byte[] bytes)
    {
        final ClassNode node = new ClassNode();
        final ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);

        return node;
    }

    private byte[] finishInjection(ClassNode node)
    {
        final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    private MethodNode getMethod(ClassNode node, String name, String sig)
    {
        for (MethodNode methodNode : node.methods)
        {
            if (methodNode.name.equals(name) && methodNode.desc.equals(sig))
            {
                return methodNode;
            }
        }
        return null;
    }
}
