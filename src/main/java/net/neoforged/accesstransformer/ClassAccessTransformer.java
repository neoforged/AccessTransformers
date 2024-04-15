package net.neoforged.accesstransformer;

import net.neoforged.accesstransformer.parser.Target;
import net.neoforged.accesstransformer.parser.Transformation;
import org.objectweb.asm.tree.ClassNode;

import java.util.Set;

public class ClassAccessTransformer extends AccessTransformer<ClassNode> {
    public ClassAccessTransformer(final Target.ClassTarget target, final Transformation transformation) {
        super(target, transformation);
    }

    @Override
    public String targetName() {
        return this.getClassName();
    }

    @Override
    protected void apply(final ClassNode node, Set<String> privateChanged) {
        node.access = mergeWith(node.access, getTransformation().modifier(), getTransformation().finalState());

        node.innerClasses.stream().filter(c -> c.name.equals(node.name)).forEach(inner -> {
            inner.access = mergeWith(inner.access, getTransformation().modifier(), getTransformation().finalState());
        });
    }
}
