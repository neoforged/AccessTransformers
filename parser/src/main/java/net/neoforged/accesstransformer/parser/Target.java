package net.neoforged.accesstransformer.parser;

public sealed abstract class Target {
    private final String className;

    protected Target(String className) {
        this.className = className;
    }

    public final String className() {
        return className;
    }

    public static final class ClassTarget extends Target {
        public ClassTarget(String className) {
            super(className);
        }

        @Override
        public String toString() {
            return className() + " CLASS";
        }
    }

    public static final class InnerClassTarget extends Target {
        private final String innerName;

        public InnerClassTarget(String className, String innerName) {
            super(className);
            this.innerName = innerName;
        }

        public String innerName() {
            return innerName;
        }

        @Override
        public String toString() {
            int idx = innerName().lastIndexOf('$');
            return className() + " INNERCLASS " + innerName().substring(idx + 1);
        }
    }

    public static final class FieldTarget extends Target {
        private final String fieldName;

        public FieldTarget(String className, String fieldName) {
            super(className);
            this.fieldName = fieldName;
        }

        public String fieldName() {
            return fieldName;
        }

        @Override
        public String toString() {
            return className() + " FIELD " + fieldName();
        }
    }

    public static final class MethodTarget extends Target {
        private final String methodName;
        private final String methodDesc;

        public MethodTarget(String className, String methodName, String methodDesc) {
            super(className);
            this.methodName = methodName;
            this.methodDesc = methodDesc;
        }

        public String methodName() {
            return methodName;
        }

        public String methodDescriptor() {
            return methodDesc;
        }

        @Override
        public String toString() {
            return className() + " METHOD " + methodName() + methodDescriptor();
        }
    }

    public static final class WildcardMethodTarget extends Target {
        public WildcardMethodTarget(String className) {
            super(className);
        }

        @Override
        public String toString() {
            return className() + " METHODWILDCARD";
        }
    }

    public static final class WildcardFieldTarget extends Target {
        public WildcardFieldTarget(String className) {
            super(className);
        }

        @Override
        public String toString() {
            return className() + " FIELDWILDCARD";
        }
    }
}
