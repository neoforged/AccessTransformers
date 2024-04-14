package net.neoforged.accesstransformer;

import net.neoforged.accesstransformer.parser.Target;
import net.neoforged.accesstransformer.parser.Transformation;
import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.*;

public abstract class AccessTransformer<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTransformer.class);
    private static final Marker AXFORM_MARKER = MarkerFactory.getMarker("AXFORM");
    private final Type type;
    private final Transformation transformation;
    private final Target target;

    public AccessTransformer(Target target, Transformation transformation) {
        this.transformation = transformation;
        this.target = target;
        this.type = Type.getType("L" + target.className().replace('.', '/') + ";");
    }

    public static AccessTransformer<?> of(Target target, Transformation transformation) {
        if (target instanceof Target.MethodTarget methodTarget) {
            return new MethodAccessTransformer(methodTarget, transformation);
        } else if (target instanceof Target.FieldTarget fieldTarget) {
            return new FieldAccessTransformer(fieldTarget, transformation);
        } else if (target instanceof Target.ClassTarget classTarget) {
            return new ClassAccessTransformer(classTarget, transformation);
        } else if (target instanceof Target.InnerClassTarget innerClassTarget) {
            return new InnerClassAccessTransformer(innerClassTarget, transformation);
        } else if (target instanceof Target.WildcardFieldTarget wildcardFieldTarget) {
            return new WildcardAccessTransformer(wildcardFieldTarget, transformation);
        } else if (target instanceof Target.WildcardMethodTarget wildcardMethodTarget) {
            return new WildcardAccessTransformer(wildcardMethodTarget, transformation);
        } else {
            // It's sealed, this shouldn't happen - we'll want to make this whole thing a switch with J21
            throw new IllegalArgumentException("Unknown target type: " + target.getClass());
        }
    }

    public TargetType getType() {
        return TargetType.CLASS;
    }

    public String getClassName() {
        return target.className();
    }

    public final Type getASMType() {
        return type;
    }

    public final Transformation getTransformation() {
        return transformation;
    }

    @Override
    public String toString() {
        return target + " " + transformation;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof AccessTransformer<?> at2)) return false;
        return Objects.equals(target, at2.target) &&
               Objects.equals(transformation, at2.transformation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, transformation);
    }

    private static final Transformation.Modifier[] lookup = new Transformation.Modifier[4];
    private static int accFlag(Transformation.Modifier modifier) {
        return switch (modifier) {
            case PUBLIC -> Opcodes.ACC_PUBLIC;
            case PROTECTED -> Opcodes.ACC_PROTECTED;
            case PRIVATE -> Opcodes.ACC_PRIVATE;
            case DEFAULT -> 0;
        };
    }

    static {
        Arrays.stream(Transformation.Modifier.values()).forEach(m->lookup[firstBit(accFlag(m))] = m);
    }

    private static int firstBit(int flags) {
        return flags == 0 ? 0 : firstBit(flags >>> 1) + 1;
    }

    private static int mergeWith(int access, Transformation.Modifier modifier) {
        Transformation.Modifier floor = lookup[firstBit(access & 7)];
        return (access & ~7) | accFlag(Transformation.Modifier.values()[Math.min(floor.ordinal(), modifier.ordinal())]);
    }

    private static int mergeWith(int access, Transformation.FinalState finalState) {
        return switch (finalState) {
            case LEAVE, CONFLICT -> access;
            case MAKEFINAL -> access | Opcodes.ACC_FINAL;
            case REMOVEFINAL -> access & ~Opcodes.ACC_FINAL;
        };
    }

    public static int mergeWith(int access, Transformation.Modifier modifier, Transformation.FinalState finalState) {
        return mergeWith(mergeWith(access, modifier), finalState);
    }

    public abstract String targetName();
    protected abstract void apply(final T node, Set<String> privateChanged);

    @SuppressWarnings("unchecked")
    public static <T> void applyTransform(final AccessTransformer<?> accessTransformer, final T node, final Set<String> privateChanged) {
        LOGGER.debug(AXFORM_MARKER,"Transforming {} to access {} and {}", accessTransformer, accessTransformer.transformation.modifier(), accessTransformer.transformation.finalState());
        ((AccessTransformer<T>) accessTransformer).apply(node, privateChanged);
    }
}
