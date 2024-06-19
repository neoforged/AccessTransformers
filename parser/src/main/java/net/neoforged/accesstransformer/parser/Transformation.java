package net.neoforged.accesstransformer.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Transformation {
    private final FinalState finalState;
    private final Modifier modifier;
    private final List<String> origins = new ArrayList<>();

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

    public List<String> getOrigins() {
        return Collections.unmodifiableList(origins);
    }

    @Override
    public String toString() {
        return modifier + " " + finalState + " " + String.join(", ", origins);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Transformation transformation)) return false;
        return finalState == transformation.finalState && modifier == transformation.modifier;
    }

    @Override
    public int hashCode() {
        return Objects.hash(finalState, modifier);
    }
}
