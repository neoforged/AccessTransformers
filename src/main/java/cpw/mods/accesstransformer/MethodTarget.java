package cpw.mods.accesstransformer;

import org.objectweb.asm.*;

import java.util.*;
import java.util.stream.*;

public class MethodTarget extends Target {
    private final String methodName;
    private final List<Type> arguments;
    private final Type returnType;

    public MethodTarget(final String className, final String methodName, final List<String> arguments, final String returnValue) {
        super(className);
        this.methodName = methodName;
        this.arguments = arguments.stream().map(s->s.replaceAll("\\.", "/")).map(Type::getType).collect(Collectors.toList());
        this.returnType = Type.getType(returnValue.replaceAll("\\.","/"));
    }

    @Override
    protected TargetType getType() {
        return TargetType.METHOD;
    }

    @Override
    public String toString() {
        return super.toString() + " " + Objects.toString(methodName);
    }
}
