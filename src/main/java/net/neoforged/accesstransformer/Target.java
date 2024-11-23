package net.neoforged.accesstransformer;

import net.neoforged.accesstransformer.api.TargetType;
import org.objectweb.asm.*;

import java.util.*;

public abstract class Target<T> {
    private final String className;
    private Type type;

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

    /**
     * Checks whether this target matches the given target description.
     *
     * @param className The FQN of the class containing the target
     * @param type The type of the target
     * @param targetName The name of the target (ignored for {@link TargetType#CLASS} and wildcard targets)
     * @return whether this target matches the given target description
     */
    public abstract boolean matches(final String className, final TargetType type, final String targetName);

    public abstract void apply(final T node, final AccessTransformer.Modifier targetAccess, final AccessTransformer.FinalState targetFinalState, Set<String> privateChanged);
}
