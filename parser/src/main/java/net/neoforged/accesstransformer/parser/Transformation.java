package net.neoforged.accesstransformer.parser;

import java.util.ArrayList;
import java.util.List;

public class Transformation {
    private FinalState finalState;
    private Modifier modifier;
    private List<String> origins = new ArrayList<>();

    private Transformation(Modifier modifier, FinalState finalState) {
        this.finalState = finalState;
        this.modifier = modifier;
    }

    public Transformation(Modifier modifier, FinalState finalState, String origin, int lineNumber) {
        this(modifier, finalState);
        this.origins.add(origin + ":" + lineNumber);
    }

    public FinalState finalState() {
        return finalState;
    }

    public Modifier modifier() {
        return modifier;
    }

    public List<String> origins() {
        return origins;
    }

    public enum FinalState {
        LEAVE, MAKEFINAL, REMOVEFINAL, CONFLICT
    }

    public enum Modifier {
        PUBLIC, PROTECTED, DEFAULT, PRIVATE
    }

    public Transformation mergeStates(final Transformation other) {
        final Modifier newModifier = Modifier.values()[Math.min(this.modifier.ordinal(), other.modifier.ordinal())];
        final FinalState newFinalState = FinalState.values()[this.finalState.ordinal() | other.finalState.ordinal()];
        final Transformation transformation = new Transformation(newModifier, newFinalState);
        transformation.origins.addAll(this.origins);
        transformation.origins.addAll(other.origins);
        return transformation;
    }

    public boolean isValid() {
        return finalState != FinalState.CONFLICT;
    }

    @Override
    public String toString() {
        return modifier + " " + finalState + " " + String.join(", ", origins);
    }
}
