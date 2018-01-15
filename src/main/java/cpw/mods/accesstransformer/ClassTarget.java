package cpw.mods.accesstransformer;

import org.objectweb.asm.tree.*;

import java.util.*;

public class ClassTarget extends Target<ClassNode> {
    public ClassTarget(final String className) {
        super(className);
    }

    @Override
    public String targetName() {
        return this.getClassName();
    }

    @Override
    public void apply(final ClassNode node, final AccessTransformer.Modifier targetAccess, final AccessTransformer.FinalState targetFinalState, Set<String> privateChanged) {
        node.access = targetAccess.mergeWith(node.access);
        node.access = targetFinalState.mergeWith(node.access);
    }
}
