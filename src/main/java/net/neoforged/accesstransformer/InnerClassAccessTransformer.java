package net.neoforged.accesstransformer;

import net.neoforged.accesstransformer.parser.Target;
import net.neoforged.accesstransformer.parser.Transformation;
import org.objectweb.asm.tree.ClassNode;

import java.util.Set;

public class InnerClassAccessTransformer extends AccessTransformer<ClassNode> {
    private final String innerName;

    public InnerClassAccessTransformer(final Target.InnerClassTarget target, final Transformation transformation) {
        super(target, transformation);
        this.innerName = target.innerName().replace('.', '/');
    }

    @Override
    public String targetName() {
        return this.innerName;
    }

    @Override
    protected void apply(final ClassNode node, Set<String> privateChanged) {
        node.innerClasses.stream().filter(c -> c.name.equals(innerName)).forEach(inner -> {
            inner.access = mergeWith(inner.access, getTransformation().modifier(), getTransformation().finalState());
        });
    }
}
