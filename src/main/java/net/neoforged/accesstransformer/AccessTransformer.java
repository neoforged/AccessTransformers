package net.neoforged.accesstransformer;

import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public class AccessTransformer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTransformer.class);
    private static final Marker AXFORM_MARKER = MarkerFactory.getMarker("AXFORM");
    private final Target<?> memberTarget;
    private final Modifier targetAccess;
    private final FinalState targetFinalState;
    private final List<String> origins = new ArrayList<>(1);

    public AccessTransformer(final Target<?> target, final Modifier modifier, final FinalState finalState, String origin, final int lineNumber) {
        this.memberTarget = target;
        this.targetAccess = modifier;
        this.targetFinalState = finalState;
        this.origins.add(origin+":"+lineNumber);
    }

    @SuppressWarnings("unchecked")
    public <T> Target<T> getTarget() {
        return (Target<T>)this.memberTarget;
    }

    public AccessTransformer mergeStates(final AccessTransformer at2, final String resourceName) {
        final Modifier newModifier = Modifier.values()[Math.min(this.targetAccess.ordinal(), at2.targetAccess.ordinal())];
        final FinalState newFinalState = FinalState.values()[this.targetFinalState.ordinal() | at2.targetFinalState.ordinal()];
        final AccessTransformer accessTransformer = new AccessTransformer(memberTarget, newModifier, newFinalState, resourceName + ":merge", 0);
        accessTransformer.origins.addAll(this.origins);
        accessTransformer.origins.addAll(at2.origins);
        return accessTransformer;
    }

    public boolean isValid() {
        return targetFinalState != FinalState.CONFLICT;
    }

    public List<String> getOrigins() {
        return Collections.unmodifiableList(origins);
    }

    public <T> void applyModifier(final T node, final Class<T> type, final Set<String> privateChanged) {
        LOGGER.debug(AXFORM_MARKER,"Transforming {} to access {} and {}", getTarget(), targetAccess, targetFinalState);
        getTarget().apply(node, targetAccess, targetFinalState, privateChanged);
    }

    public enum Modifier {
        PUBLIC(Opcodes.ACC_PUBLIC), PROTECTED(Opcodes.ACC_PROTECTED), DEFAULT(0), PRIVATE(Opcodes.ACC_PRIVATE);
    	private static final Modifier[] lookup = new Modifier[4];
        private final int accFlag;

        static {
            Arrays.stream(Modifier.values()).forEach(m->lookup[firstBit(m.accFlag)] = m);
        }

        Modifier(final int accFlag) {
            this.accFlag = accFlag;
        }

        private static int firstBit(int flags) {
            return flags == 0 ? 0 : firstBit(flags >>> 1) + 1;
        }

        public int mergeWith(final int access) {
            Modifier floor = lookup[firstBit(access & 7)];
            return (access & ~7) | values()[Math.min(floor.ordinal(), this.ordinal())].accFlag;
        }
    }

    public enum FinalState {
        LEAVE(i->i), MAKEFINAL(i->i | Opcodes.ACC_FINAL), REMOVEFINAL(i->i & ~Opcodes.ACC_FINAL), CONFLICT(i->i);
        private IntFunction<Integer> function;

        FinalState(final IntFunction<Integer> function) {
            this.function = function;
        }

        public int mergeWith(final int access) {
            return function.apply(access);
        }
    }

    @Override
    public String toString() {
        return Objects.toString(memberTarget) + " " + Objects.toString(targetAccess) + " " + Objects.toString(targetFinalState) + " " + Objects.toString(origins.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }
}
