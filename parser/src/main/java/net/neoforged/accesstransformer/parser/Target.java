package net.neoforged.accesstransformer.parser;

import java.util.Objects;

public abstract class Target {
    private final String className;

    Target(String className) {
        this.className = className;
    }

    public String className() {
        return this.className;
    }

    /**
     * Checks whether this target matches the given target description.
     *
     * @param className The FQN of the class containing the target
     * @param type The type of the target
     * @param targetName The name of the target (ignored for {@link TargetType#CLASS} and wildcard targets)
     * @return whether this target matches the given target description
     */
    public abstract boolean matches(final String className, final TargetType type, final String targetName);

    public static class ClassTarget extends Target {
        public ClassTarget(String className) { super(className); }
        @Override public String toString() { return className() + " CLASS"; }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ClassTarget)) return false;
            ClassTarget that = (ClassTarget) o;
            return Objects.equals(className(), that.className());
        }
        @Override public int hashCode() {
            return Objects.hash(className());
        }

        @Override
        public boolean matches(final String className, final TargetType type, final String targetName) {
            return type == TargetType.CLASS && this.className().equals(className);
        }
    }

    public static class InnerClassTarget extends Target {
        private final String innerName;
        public InnerClassTarget(String className, String innerName) {
            super(className);
            this.innerName = innerName;
        }
        public String innerName() { return innerName; }
        @Override public String toString() {
            int idx = innerName().lastIndexOf('$');
            return className() + " INNERCLASS " + innerName().substring(idx + 1);
        }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof InnerClassTarget)) return false;
            InnerClassTarget that = (InnerClassTarget) o;
            return Objects.equals(className(), that.className()) &&
                   Objects.equals(innerName, that.innerName);
        }
        @Override public int hashCode() {
            return Objects.hash(className(), innerName);
        }

        @Override
        public boolean matches(final String className, final TargetType type, final String targetName) {
            return type == TargetType.CLASS && this.innerName.equals(className);
        }
    }

    public static class FieldTarget extends Target {
        private final String fieldName;
        public FieldTarget(String className, String fieldName) {
            super(className);
            this.fieldName = fieldName;
        }
        public String fieldName() { return fieldName; }
        @Override public String toString() { return className() + " FIELD " + fieldName(); }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FieldTarget)) return false;
            FieldTarget that = (FieldTarget) o;
            return Objects.equals(className(), that.className()) &&
                   Objects.equals(fieldName, that.fieldName);
        }
        @Override public int hashCode() {
            return Objects.hash(className(), fieldName);
        }

        @Override
        public boolean matches(final String className, final TargetType type, final String targetName) {
            return type == TargetType.FIELD && this.className().equals(className) && this.fieldName.equals(targetName);
        }
    }

    public static class MethodTarget extends Target {
        private final String methodName;
        private final String methodDescriptor;
        public MethodTarget(String className, String methodName, String methodDescriptor) {
            super(className);
            this.methodName = methodName;
            this.methodDescriptor = methodDescriptor;
        }
        public String methodName() { return methodName; }
        public String methodDescriptor() { return methodDescriptor; }
        @Override public String toString() {
            return className() + " METHOD " + methodName() + methodDescriptor();
        }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MethodTarget)) return false;
            MethodTarget that = (MethodTarget) o;
            return Objects.equals(className(), that.className()) &&
                   Objects.equals(methodName, that.methodName) &&
                   Objects.equals(methodDescriptor, that.methodDescriptor);
        }
        @Override public int hashCode() {
            return Objects.hash(className(), methodName, methodDescriptor);
        }

        @Override
        public boolean matches(final String className, final TargetType type, final String targetName) {
            return type == TargetType.METHOD && this.className().equals(className) && targetName.equals(this.methodName + this.methodDescriptor);
        }
    }

    public static class WildcardMethodTarget extends Target {
        public WildcardMethodTarget(String className) { super(className); }
        @Override public String toString() { return className() + " METHODWILDCARD"; }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof WildcardMethodTarget)) return false;
            WildcardMethodTarget that = (WildcardMethodTarget) o;
            return Objects.equals(className(), that.className());
        }
        @Override public int hashCode() {
            return Objects.hash(className());
        }

        @Override
        public boolean matches(final String className, final TargetType type, final String targetName) {
            return type == TargetType.METHOD && this.className().equals(className);
        }
    }

    public static class WildcardFieldTarget extends Target {
        public WildcardFieldTarget(String className) { super(className); }
        @Override public String toString() { return className() + " FIELDWILDCARD"; }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof WildcardFieldTarget)) return false;
            WildcardFieldTarget that = (WildcardFieldTarget) o;
            return Objects.equals(className(), that.className());
        }
        @Override public int hashCode() {
            return Objects.hash(className());
        }

        @Override
        public boolean matches(final String className, final TargetType type, final String targetName) {
            return type == TargetType.FIELD && this.className().equals(className);
        }
    }
}
