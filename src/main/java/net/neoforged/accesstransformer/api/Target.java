package net.neoforged.accesstransformer.api;

import org.objectweb.asm.*;

import java.util.*;

public abstract class Target<T> {
    private final String className;
    private final Type type;

    public Target(String className) {
        this.className = className;
        this.type = Type.getType("L" + className.replaceAll("\\.", "/") + ";");
    }

    public TargetType getType() {
        return TargetType.CLASS;
    }

    public String getClassName() {
        return className;
    }

    public final Type getASMType() {
        return type;
    }
    @Override
    public String toString() {
        return Objects.toString(className) + " " + Objects.toString(getType());
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Target)) return false;
        return Objects.equals(className, ((Target<?>)obj).className) &&
               Objects.equals(getType(), ((Target<?>)obj).getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClassName(), getType(), "CLASS");
    }

    public abstract String targetName();
    public abstract void apply(final T node, final AccessTransformer.Modifier targetAccess, final AccessTransformer.FinalState targetFinalState, Set<String> privateChanged);

    /**
     * {@return the targets this target applies to as a wildcard, or {@code null} if it is not a wildcard}
     */
    public TargetType getWildcardTarget() {
        return null;
    }
}
