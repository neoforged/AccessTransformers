package cpw.mods.accesstransformer;

import java.util.*;
import java.util.function.*;

public class WildcardTarget extends Target {
    private final TargetType type;

    public WildcardTarget(String targetName, boolean isMethod) {
        super(targetName);
        this.type = isMethod ? TargetType.METHOD : TargetType.FIELD;
    }
    @Override
    public TargetType getType() {
        return type;
    }

    @Override
    public String toString() {
        return super.toString() + "WILDCARD";
    }

    @Override
    public boolean equals(final Object obj) {
        return super.equals(obj) && obj instanceof WildcardTarget;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClassName(), getType(), "WILDCARD");
    }
}
