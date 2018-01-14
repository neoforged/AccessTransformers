package cpw.mods.accesstransformer;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

public class Target {
    private final String className;


    public Target(String className) {
        this.className = className;
    }

    public TargetType getType() {
        return TargetType.CLASS;
    }

    public String getClassName() {
        return className;
    }

    public final Type getASMType() {
        return Type.getType(className);
    }
    @Override
    public String toString() {
        return Objects.toString(className) + " " + Objects.toString(getType());
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Target)) return false;
        return Objects.equals(className, ((Target)obj).className) &&
               Objects.equals(getType(), ((Target)obj).getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClassName(), getType(), "CLASS");
    }


    public <T> boolean findAndApplyToNode(ClassNode node, Function<T, Void> action) {
        @SuppressWarnings("unchecked")
        final Function<ClassNode, Void> action1 = (Function<ClassNode, Void>) action;
        action1.apply(node);
        return true;
    }
}
