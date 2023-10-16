package net.neoforged.accesstransformer;

import org.objectweb.asm.tree.*;

import java.util.*;

public class InnerClassTarget extends Target<ClassNode> {
    private final String innerName;

    public InnerClassTarget(final String className, final String innerName) {
        super(className);
        this.innerName = innerName;
    }

    @Override
    public String targetName() {
        return this.innerName;
    }
    @Override
    public String toString() {
        int idx = innerName.lastIndexOf('$');
        return Objects.toString(getClassName()) + " INNERCLASS " + Objects.toString(innerName.substring(idx + 1));
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof InnerClassTarget)) return false;
        return super.equals(obj) &&
               Objects.equals(innerName, ((InnerClassTarget)obj).innerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClassName(), getType(), innerName);
    }

    @Override
    public void apply(final ClassNode node, final AccessTransformer.Modifier targetAccess, final AccessTransformer.FinalState targetFinalState, Set<String> privateChanged) {
        node.innerClasses.stream().filter(c -> c.name.equals(innerName)).forEach(inner -> {
            inner.access = targetAccess.mergeWith(inner.access);
            inner.access = targetFinalState.mergeWith(inner.access);
        });
    }
}
