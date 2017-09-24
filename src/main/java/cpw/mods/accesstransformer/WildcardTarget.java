package cpw.mods.accesstransformer;

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
}
