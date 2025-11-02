package net.neoforged.accesstransformer;

import net.neoforged.accesstransformer.parser.Target;
import net.neoforged.accesstransformer.parser.TargetType;
import net.neoforged.accesstransformer.parser.Transformation;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public abstract class AccessTransformer<T> {
    private final Transformation transformation;
    private final Target target;

    public AccessTransformer(Target target, Transformation transformation) {
        this.transformation = transformation;
        this.target = target;
    }

    public static AccessTransformer<?> of(Target target, Transformation transformation) {
        if (target instanceof Target.MethodTarget) {
            return new MethodAccessTransformer((Target.MethodTarget) target, transformation);
        } else if (target instanceof Target.FieldTarget) {
            return new FieldAccessTransformer((Target.FieldTarget) target, transformation);
        } else if (target instanceof Target.ClassTarget) {
            return new ClassAccessTransformer((Target.ClassTarget) target, transformation);
        } else if (target instanceof Target.InnerClassTarget) {
            return new InnerClassAccessTransformer((Target.InnerClassTarget) target, transformation);
        } else if (target instanceof Target.WildcardFieldTarget) {
            return new WildcardAccessTransformer((Target.WildcardFieldTarget) target, transformation);
        } else if (target instanceof Target.WildcardMethodTarget) {
            return new WildcardAccessTransformer((Target.WildcardMethodTarget) target, transformation);
        } else {
            throw new IllegalArgumentException("Unknown target type: " + target.getClass());
        }
    }

    public TargetType getType() {
        return TargetType.CLASS;
    }

    public String getClassName() {
        return target.className();
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
        if (!(obj instanceof AccessTransformer)) return false;
        AccessTransformer<?> at2 = (AccessTransformer<?>) obj;
        return Objects.equals(target, at2.target) &&
               Objects.equals(transformation, at2.transformation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, transformation);
    }

    private static final Transformation.Modifier[] lookup = new Transformation.Modifier[4];
    private static int accFlag(Transformation.Modifier modifier) {
        switch (modifier) {
            case PUBLIC:
                return Opcodes.ACC_PUBLIC;
            case PROTECTED:
                return Opcodes.ACC_PROTECTED;
            case PRIVATE:
                return Opcodes.ACC_PRIVATE;
            case DEFAULT:
                return 0;
            default:
                throw new IllegalArgumentException("Unknown modifier: " + modifier);
        }
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
        switch (finalState) {
            case LEAVE:
                return access;
            case CONFLICT:
                return access;
            case MAKEFINAL:
                return access | Opcodes.ACC_FINAL;
            case REMOVEFINAL:
                return access & ~Opcodes.ACC_FINAL;
            default:
                throw new IllegalArgumentException("Unknown final state: " + finalState);
        }
    }

    public static int mergeWith(int access, Transformation.Modifier modifier, Transformation.FinalState finalState) {
        return mergeWith(mergeWith(access, modifier), finalState);
    }

    public abstract String targetName();
    protected abstract void apply(final T node, Set<String> privateChanged);

    @SuppressWarnings("unchecked")
    public static <T> void applyTransform(final AccessTransformer<?> accessTransformer, final T node, final Set<String> privateChanged) {
        ((AccessTransformer<T>) accessTransformer).apply(node, privateChanged);
    }
}
