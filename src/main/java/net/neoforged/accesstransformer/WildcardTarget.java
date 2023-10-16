package net.neoforged.accesstransformer;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.*;

public class WildcardTarget extends Target<ClassNode> {
    private final TargetType type;

    public WildcardTarget(String targetName, boolean isMethod) {
        super(targetName);
        this.type = isMethod ? TargetType.METHOD : TargetType.FIELD;
    }
    @Override
    // We target CLASS because we process classnodes
    public TargetType getType() {
        return TargetType.CLASS;
    }

    @Override
    public String toString() {
        return Objects.toString(getClassName()) + " " + type + "WILDCARD";
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof WildcardTarget)) return false;
        return super.equals(obj) &&
                ((WildcardTarget)obj).type == type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClassName(), getType(), "WILDCARD", type);
    }

    @Override
    public String targetName() {
        return "*"+ type + "*";
    }

    @Override
    public void apply(final ClassNode node, final AccessTransformer.Modifier targetAccess, final AccessTransformer.FinalState targetFinalState, Set<String> privateChanged) {
        if (this.type == TargetType.FIELD) {
            for (FieldNode fn : node.fields) {
                fn.access = targetAccess.mergeWith(fn.access);
                fn.access = targetFinalState.mergeWith(fn.access);
            }
        } else if (this.type == TargetType.METHOD) {
            for (MethodNode mn : node.methods) {
                boolean wasPrivate = (mn.access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE;
                mn.access = targetAccess.mergeWith(mn.access);
                mn.access = targetFinalState.mergeWith(mn.access);
                if (wasPrivate && !"<init>".equals(mn.name) && (mn.access & Opcodes.ACC_PRIVATE) != Opcodes.ACC_PRIVATE) {
                    privateChanged.add(mn.name+mn.desc);
                }
            }
        }
    }
}
