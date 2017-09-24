package cpw.mods.accesstransformer;

import org.apache.logging.log4j.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.*;
import java.util.stream.*;

public class AccessTransformer {
    private static final Logger LOG = LogManager.getLogger();
    private final Target memberTarget;
    private final Modifier targetAccess;
    private final FinalState targetFinalState;
    private final List<String> origins = new ArrayList<>(1);

    public AccessTransformer(final Target target, final Modifier modifier, final FinalState finalState, String origin, final int lineNumber) {
        this.memberTarget = target;
        this.targetAccess = modifier;
        this.targetFinalState = finalState;
        this.origins.add(origin+":"+lineNumber);
    }

    public Target getTarget() {
        return this.memberTarget;
    }

    public AccessTransformer mergeStates(final AccessTransformer at2, final String resourceName) {
        final Modifier newModifier = Modifier.values()[Math.min(this.targetAccess.ordinal(), at2.targetAccess.ordinal())];
        final FinalState newFinalState = FinalState.values()[this.targetFinalState.ordinal() & at2.targetFinalState.ordinal()];
        final AccessTransformer accessTransformer = new AccessTransformer(memberTarget, newModifier, newFinalState, resourceName + ":merge", 0);
        accessTransformer.origins.addAll(this.origins);
        accessTransformer.origins.addAll(at2.origins);
        return accessTransformer;
    }

    public boolean isValid() {
        return targetFinalState != FinalState.CONFLICT;
    }

    public List<String> getOrigins() {
        return origins;
    }

    public void applyModifier(final ClassNode clazzNode) {
        getTarget().findAndApplyToNode(clazzNode, (ClassNode cn) -> {
            LOG.debug("Modifying access of {} to {}", this::getTarget, ()->targetAccess);
            clazzNode.access = targetAccess.mergeWith(clazzNode.access);
            return null;
        });
    }

    public enum Modifier {
        PUBLIC(Opcodes.ACC_PUBLIC), PROTECTED(Opcodes.ACC_PROTECTED), DEFAULT(0), PRIVATE(Opcodes.ACC_PRIVATE);
        private final int accFlag;

        Modifier(final int accFlag) {
            this.accFlag = accFlag;
        }

        public int mergeWith(final int access) {
            final int previousAccess = access & ~7;
            return previousAccess | accFlag;
        }
    }

    public enum FinalState {
        LEAVE, MAKEFINAL, REMOVEFINAL, CONFLICT
    }

    @Override
    public String toString() {
        return Objects.toString(memberTarget) + " " + Objects.toString(targetAccess) + " " + Objects.toString(targetFinalState) + " " + Objects.toString(origins.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }
}
