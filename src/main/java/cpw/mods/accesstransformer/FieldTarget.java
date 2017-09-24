package cpw.mods.accesstransformer;

import java.util.*;

public class FieldTarget extends Target {
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
}
