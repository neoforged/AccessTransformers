package net.neoforged.accesstransformer;

import net.neoforged.accesstransformer.parser.Target;
import net.neoforged.accesstransformer.parser.Transformation;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

import java.util.Set;

public class MethodAccessTransformer extends AccessTransformer<MethodNode> {
    private final String targetName;

    public MethodAccessTransformer(Target.MethodTarget target, final Transformation transformation) {
        super(target, transformation);
        this.targetName = target.methodName()+target.methodDescriptor();
    }

    @Override
    public TargetType getType() {
        return TargetType.METHOD;
    }

    @Override
    public String targetName() {
        return targetName;
    }

    @Override
    protected void apply(final MethodNode node, Set<String> privateChanged) {
        boolean wasPrivate = (node.access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE;
        node.access = mergeWith(node.access, getTransformation().modifier(), getTransformation().finalState());
        if (wasPrivate && !"<init>".equals(node.name) && (node.access & Opcodes.ACC_PRIVATE) != Opcodes.ACC_PRIVATE) {
            privateChanged.add(node.name+node.desc);
        }
    }
}
