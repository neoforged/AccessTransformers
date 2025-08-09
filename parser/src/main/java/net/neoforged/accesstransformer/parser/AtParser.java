package net.neoforged.accesstransformer.parser;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public final class AtParser {
    private AtParser() {}

    public static void parse(Reader wrappedReader, String originName, BiConsumer<Target, Transformation> consumer) throws IOException {
        try (LineNumberReader reader = new LineNumberReader(wrappedReader)) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Strip comments and split on groups of spaces, avoiding empty parts
                StringBuilder builder = new StringBuilder();
                List<String> parts = new ArrayList<>();
                builder.setLength(0);
                for (char c : line.toCharArray()) {
                    if (Character.isWhitespace(c)) {
                        if (builder.length() != 0) {
                            parts.add(builder.toString());
                        }
                        builder.setLength(0);
                    } else if (c == '#') {
                        builder.setLength(0);
                        break;
                    } else {
                        builder.appendCodePoint(c);
                    }
                }
                if (builder.length() != 0) {
                    parts.add(builder.toString());
                }
                if (parts.isEmpty()) {
                    // The line is empty or all comments
                    continue;
                }
                if (parts.size() < 2) {
                    // The line has something in the "modifier" slot but nothing else
                    throw new RuntimeException("Invalid line " + reader.getLineNumber() + "; should be '<modifier> <class name> [<member>]'");
                }

                String modifierString = parts.get(0);
                Transformation.FinalState finalState = Transformation.FinalState.LEAVE;
                if (modifierString.endsWith("-f")) {
                    finalState = Transformation.FinalState.REMOVEFINAL;
                    modifierString = modifierString.substring(0, modifierString.length() - 2);
                } else if (modifierString.endsWith("+f")) {
                    finalState = Transformation.FinalState.MAKEFINAL;
                    modifierString = modifierString.substring(0, modifierString.length() - 2);
                }
                Transformation.Modifier modifier = parseModifier(modifierString, reader.getLineNumber());

                String className = parts.get(1);
                // Validate class name as dot-separated java identifiers
                if (className.chars().reduce(0, (last, current) -> {
                    if (last == 0 && !Character.isJavaIdentifierStart(current) ||
                            last != 0 && current != '.' && !Character.isJavaIdentifierPart(current)
                    ) {
                        throw new RuntimeException("Invalid class name '" + className + "' at line " + reader.getLineNumber());
                    }
                    return current == '.' ? 0 : 1;
                }) != 1) {
                    throw new RuntimeException("Invalid class name '" + className + "' at line " + reader.getLineNumber());
                }

                Transformation transformation = new Transformation(modifier, finalState, originName, reader.getLineNumber());

                Target target;

                if (parts.size() < 3) {
                    target = new Target.ClassTarget(className);
                    // Class ATs of inner classes have a corresponding inner class AT for the nesting class
                    locateInnerClassAts(className, transformation, consumer);
                } else {
                    String member = parts.get(2);
                    if (member.equals("*")) {
                        target = new Target.WildcardFieldTarget(className);
                    } else if (member.equals("*()")) {
                        target = new Target.WildcardMethodTarget(className);
                    } else if (member.contains("(")) {
                        String name = member.substring(0, member.indexOf('('));
                        // For some reason old forge ATs let you use descriptors with dots instead of slashes
                        // We replicate this behavior here
                        String desc = member.substring(member.indexOf('('))
                                .replace('.', '/');
                        validateMethodDescriptor(desc, reader.getLineNumber());
                        if (!name.equals("<init>")) {
                            validateIdentifier(name, "method", reader.getLineNumber());
                        }
                        target = new Target.MethodTarget(className, name, desc);
                    } else {
                        validateIdentifier(member, "field", reader.getLineNumber());
                        target = new Target.FieldTarget(className, member);
                    }
                }
                consumer.accept(target, transformation);
            }
        }
    }

    private static Transformation.Modifier parseModifier(String modifier, int line) {
        // Java 8: switch statement instead of switch expression
        switch (modifier) {
            case "public":
                return Transformation.Modifier.PUBLIC;
            case "private":
                return Transformation.Modifier.PRIVATE;
            case "protected":
                return Transformation.Modifier.PROTECTED;
            case "default":
                return Transformation.Modifier.DEFAULT;
            default:
                throw new RuntimeException("Invalid modifier: " + modifier + " at line " + line);
        }
    }

    private static void locateInnerClassAts(String className, Transformation transformation, BiConsumer<Target, Transformation> consumer) {
        int split = className.lastIndexOf('$');
        if (split == -1) {
            return;
        }
        String parent = className.substring(0, split);
        consumer.accept(new Target.InnerClassTarget(parent, className), transformation);
    }

    private static void validateMethodDescriptor(String desc, int index) {
        int open = desc.indexOf('(');
        int close = desc.lastIndexOf(')');
        String error = "Invalid method descriptor '"+ desc +"' at line " + index;
        if (open != 0 || close == -1) {
            throw new RuntimeException(error);
        }
        String argsTypes = desc.substring(open + 1, close);
        String returnType = desc.substring(close + 1);
        if (validateDescriptor(returnType, true, error) != 1) {
            throw new RuntimeException(error);
        }
        validateDescriptor(argsTypes, false, error);
    }

    private static int validateDescriptor(String types, boolean allowVoid, String error) {
        int count = 0;
        int idx = 0;
        boolean inArray = false;
        while (idx < types.length()) {
            char c = types.charAt(idx);
            if (c == 'V' && (!allowVoid || inArray)) {
                throw new RuntimeException(error);
            } else if ("ZCBSIFJDV".indexOf(c) != -1) {
                inArray = false;
                count++;
            } else if (c == '[') {
                inArray = true;
            } else if (c == 'L') {
                int end = types.indexOf(';', idx);
                if (end == -1) {
                    throw new RuntimeException(error);
                }
                String full = types.substring(idx+1, end);
                if (full.chars().anyMatch(i -> ".[".indexOf(i) != -1)) {
                    throw new RuntimeException(error);
                }
                count++;
                inArray = false;
                idx = end;
            } else {
                throw new RuntimeException(error);
            }
            idx++;
        }
        if (inArray) {
            throw new RuntimeException(error);
        }
        return count;
    }

    private static void validateIdentifier(String name, String sort, int index) {
        if (!Character.isJavaIdentifierStart(name.charAt(0))) {
            throw new RuntimeException("Invalid " + sort + "name '" + name + "' at line " + index);
        }
        for (int i = 1; i < name.length(); i++) {
            if (!Character.isJavaIdentifierPart(name.charAt(i))) {
                throw new RuntimeException("Invalid " + sort + "name '" + name + "' at line " + index);
            }
        }
    }
}
