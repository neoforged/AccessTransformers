package net.neoforged.accesstransformer;

import net.neoforged.accesstransformer.parser.Target;
import net.neoforged.accesstransformer.parser.Transformation;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.*;

public class WildcardAccessTransformer extends AccessTransformer<ClassNode> {
    private final TargetType type;

    public WildcardAccessTransformer(Target.WildcardFieldTarget target, Transformation transformation) {
        super(target, transformation);
        this.type = TargetType.FIELD;
    }

    public WildcardAccessTransformer(Target.WildcardMethodTarget target, Transformation transformation) {
        super(target, transformation);
        this.type = TargetType.METHOD;
    }
    @Override
    // We target CLASS because we process classnodes
    public TargetType getType() {
        return TargetType.CLASS;
    }

    @Override
    public String targetName() {
        return "*"+ type + "*";
    }

    @Override
    protected void apply(final ClassNode node, Set<String> privateChanged) {
        if (this.type == TargetType.FIELD) {
            for (FieldNode fn : node.fields) {
                fn.access = mergeWith(fn.access, getTransformation().modifier(), getTransformation().finalState());
            }
        } else if (this.type == TargetType.METHOD) {
            for (MethodNode mn : node.methods) {
                boolean wasPrivate = (mn.access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE;
                mn.access = mergeWith(mn.access, getTransformation().modifier(), getTransformation().finalState());
                if (wasPrivate && !"<init>".equals(mn.name) && (mn.access & Opcodes.ACC_PRIVATE) != Opcodes.ACC_PRIVATE) {
                    privateChanged.add(mn.name+mn.desc);
                }
            }
        }
    }
}
