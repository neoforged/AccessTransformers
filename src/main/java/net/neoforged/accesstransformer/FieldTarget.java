package net.neoforged.accesstransformer;

import net.neoforged.accesstransformer.api.AccessTransformer;
import net.neoforged.accesstransformer.api.Target;
import net.neoforged.accesstransformer.api.TargetType;
import org.objectweb.asm.tree.*;

import java.util.*;

public class FieldTarget extends Target<FieldNode> {
    private final String fieldName;

    public FieldTarget(String className, String fieldName) {
        super(className);
        this.fieldName = fieldName;
    }

    @Override
    public TargetType getType() {
        return TargetType.FIELD;
    }

    @Override
    public String toString() {
        return super.toString() + " " + Objects.toString(fieldName);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof FieldTarget)) return false;
        return super.equals(obj) &&
                Objects.equals(fieldName, ((FieldTarget)obj).fieldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClassName(), getType(), fieldName);
    }

    @Override
    public void apply(final FieldNode node, final AccessTransformer.Modifier targetAccess, final AccessTransformer.FinalState targetFinalState, Set<String> privateChanged) {
        node.access = targetAccess.mergeWith(node.access);
        node.access = targetFinalState.mergeWith(node.access);
    }

    @Override
    public String targetName() {
        return getFieldName();
    }

    public String getFieldName() {
        return fieldName;
    }
}
