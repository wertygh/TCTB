package net.wertygh.tctb.core;

import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.util.EnumSet;

public class TCTBLaunchPlugin implements ILaunchPluginService {
    private static final String TARGET = "se/mickelus/tetra/items/modular/ItemModularHandheld";
    private static final String HOOKS = "net/wertygh/tctb/bridge/asm/TCTBHooks";
    private static final String SELF_PKG = "net/wertygh/tctb/";

    private static final String[] SKIP_PREFIXES = {
            "java/", "javax/", "jdk/", "sun/", "com/sun/",
            "org/objectweb/", "cpw/mods/",
            "net/minecraft/", "net/minecraftforge/",
            SELF_PKG
    };

    @Override
    public String name() {
        return "tctb_itemmodularhandheld_unify";
    }

    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty) {
        String internalName = classType.getInternalName();
        for (String prefix : SKIP_PREFIXES) {
            if (internalName.startsWith(prefix)) {
                return EnumSet.noneOf(Phase.class);
            }
        }
        return EnumSet.of(Phase.BEFORE);
    }

    @Override
    public int processClassWithFlags(Phase phase, ClassNode classNode, Type classType, String reason) {
        if (phase != Phase.BEFORE || classNode == null) {
            return ComputeFlags.NO_REWRITE;
        }
        if (classNode.name.startsWith(SELF_PKG)) {
            return ComputeFlags.NO_REWRITE;
        }

        boolean changed = false;
        for (MethodNode method : classNode.methods) {
            InsnList insns = method.instructions;
            for (AbstractInsnNode insn = insns.getFirst(); insn != null; ) {
                AbstractInsnNode next = insn.getNext();
                int op = insn.getOpcode();
                if (op == Opcodes.INSTANCEOF || op == Opcodes.CHECKCAST) {
                    TypeInsnNode t = (TypeInsnNode) insn;
                    if (TARGET.equals(t.desc)) {
                        if (op == Opcodes.INSTANCEOF) {
                            insns.set(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS,
                                    "isModularHandheldLike", "(Ljava/lang/Object;)Z", false));
                        } else {
                            insns.set(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS,
                                    "castModularHandheld", "(Ljava/lang/Object;)L" + TARGET + ";", false));
                        }
                        changed = true;
                    }
                }
                insn = next;
            }
        }
        return changed ? ComputeFlags.SIMPLE_REWRITE : ComputeFlags.NO_REWRITE;
    }
}