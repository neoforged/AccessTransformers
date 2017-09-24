package cpw.mods.accesstransformer;

import java.util.*;

public class Target {
    private final String className;


    public Target(String className) {
        this.className = className;
    }

    protected TargetType getType() {
        return TargetType.CLASS;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public String toString() {
        return Objects.toString(className) + " " + Objects.toString(getType());
    }
}
