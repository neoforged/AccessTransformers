package net.neoforged.accesstransformer;

import net.neoforged.accesstransformer.parser.Target;
import net.neoforged.accesstransformer.parser.Transformation;
import org.objectweb.asm.tree.*;

import java.util.*;

public class FieldAccessTransformer extends AccessTransformer<FieldNode> {
    private final String fieldName;

    public FieldAccessTransformer(Target.FieldTarget target, Transformation transformation) {
        super(target, transformation);
        this.fieldName = target.fieldName();
    }

    @Override
    public TargetType getType() {
        return TargetType.FIELD;
    }

    @Override
    protected void apply(final FieldNode node, Set<String> privateChanged) {
        node.access = mergeWith(node.access, getTransformation().modifier(), getTransformation().finalState());
    }

    @Override
    public String targetName() {
        return getFieldName();
    }

    public String getFieldName() {
        return fieldName;
    }
}
