package cpw.mods.accesstransformer;

public class FieldTarget extends Target {
    private final String fieldName;

    public FieldTarget(String className, String fieldName) {
        super(className);
        this.fieldName = fieldName;
    }

    @Override
    protected TargetType getType() {
        return TargetType.FIELD;
    }
}
