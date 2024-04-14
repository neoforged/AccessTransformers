package net.neoforged.accesstransformer;

import net.neoforged.accesstransformer.api.AccessTransformer;
import net.neoforged.accesstransformer.api.Target;
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

        node.innerClasses.stream().filter(c -> c.name.equals(node.name)).forEach(inner -> {
            inner.access = targetAccess.mergeWith(inner.access);
            inner.access = targetFinalState.mergeWith(inner.access);
        });
    }
}
