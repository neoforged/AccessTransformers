package net.neoforged.accesstransformer.parser;

public sealed interface Target {
    String className();

    /**
     * Checks whether this target matches the given target description.
     *
     * @param className The FQN of the class containing the target
     * @param type The type of the target
     * @param targetName The name of the target (ignored for {@link TargetType#CLASS} and wildcard targets)
     * @return whether this target matches the given target description
     */
    boolean matches(final String className, final TargetType type, final String targetName);

    record ClassTarget(String className) implements Target {
        @Override
        public String toString() {
            return className() + " CLASS";
        }

        @Override
        public boolean matches(final String className, final TargetType type, final String targetName) {
            return type == TargetType.CLASS && this.className.equals(className);
        }
    }

    record InnerClassTarget(String className, String innerName) implements Target {
        @Override
        public String toString() {
            int idx = innerName().lastIndexOf('$');
            return className() + " INNERCLASS " + innerName().substring(idx + 1);
        }

        @Override
        public boolean matches(final String className, final TargetType type, final String targetName) {
            return type == TargetType.CLASS && this.innerName.equals(className);
        }
    }

    record FieldTarget(String className, String fieldName) implements Target {
        @Override
        public String toString() {
            return className() + " FIELD " + fieldName();
        }

        @Override
        public boolean matches(final String className, final TargetType type, final String targetName) {
            return type == TargetType.FIELD && this.className.equals(className) && this.fieldName.equals(targetName);
        }
    }

    record MethodTarget(String className, String methodName, String methodDescriptor) implements Target {
        @Override
        public String toString() {
            return className() + " METHOD " + methodName() + methodDescriptor();
        }

        @Override
        public boolean matches(final String className, final TargetType type, final String targetName) {
            return type == TargetType.METHOD && this.className.equals(className) && targetName.equals(this.methodName + this.methodDescriptor);
        }
    }

    record WildcardMethodTarget(String className) implements Target {
        @Override
        public String toString() {
            return className() + " METHODWILDCARD";
        }

        @Override
        public boolean matches(final String className, final TargetType type, final String targetName) {
            return type == TargetType.METHOD && this.className.equals(className);
        }
    }

    record WildcardFieldTarget(String className) implements Target {
        @Override
        public String toString() {
            return className() + " FIELDWILDCARD";
        }

        @Override
        public boolean matches(final String className, final TargetType type, final String targetName) {
            return type == TargetType.FIELD && this.className.equals(className);
        }
    }
}
