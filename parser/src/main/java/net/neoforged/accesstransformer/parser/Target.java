package net.neoforged.accesstransformer.parser;

public sealed interface Target {
    String className();

    record ClassTarget(String className) implements Target {
        @Override
        public String toString() {
            return className() + " CLASS";
        }
    }

    record InnerClassTarget(String className, String innerName) implements Target {
        @Override
        public String toString() {
            int idx = innerName().lastIndexOf('$');
            return className() + " INNERCLASS " + innerName().substring(idx + 1);
        }
    }

    record FieldTarget(String className, String fieldName) implements Target {
        @Override
        public String toString() {
            return className() + " FIELD " + fieldName();
        }
    }

    record MethodTarget(String className, String methodName, String methodDescriptor) implements Target {
        @Override
        public String toString() {
            return className() + " METHOD " + methodName() + methodDescriptor();
        }
    }

    record WildcardMethodTarget(String className) implements Target {
        @Override
        public String toString() {
            return className() + " METHODWILDCARD";
        }
    }

    record WildcardFieldTarget(String className) implements Target {
        @Override
        public String toString() {
            return className() + " FIELDWILDCARD";
        }
    }
}
