package net.neoforged.accesstransformer.parser;

import net.neoforged.accesstransformer.*;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public final class AtParser {
    private AtParser() {}

    public static List<AccessTransformer> parse(Reader wrappedReader, String originName) throws IOException {
        try (LineNumberReader reader = new LineNumberReader(wrappedReader)) {
            List<AccessTransformer> accessTransformers = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                // Strip comments and split on groups of spaces, avoiding empty parts
                StringBuilder builder = new StringBuilder();
                List<String> parts = new ArrayList<>();
                builder.setLength(0);
                for (char c : line.toCharArray()) {
                    if (Character.isWhitespace(c)) {
                        if (!builder.isEmpty()) {
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
                if (!builder.isEmpty()) {
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
                AccessTransformer.FinalState finalState = AccessTransformer.FinalState.LEAVE;
                if (modifierString.endsWith("-f")) {
                    finalState = AccessTransformer.FinalState.REMOVEFINAL;
                    modifierString = modifierString.substring(0, modifierString.length() - 2);
                } else if (modifierString.endsWith("+f")) {
                    finalState = AccessTransformer.FinalState.MAKEFINAL;
                    modifierString = modifierString.substring(0, modifierString.length() - 2);
                }
                AccessTransformer.Modifier modifier = parseModifier(modifierString, reader.getLineNumber());

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

                Target<?> target;

                if (parts.size() < 3) {
                    target = new ClassTarget(className);
                    // Class ATs of inner classes have a corresponding inner class AT for the nesting class
                    locateInnerClassAts(className, modifier, finalState, originName, reader.getLineNumber(), accessTransformers);
                } else {
                    String member = parts.get(2);
                    if (member.equals("*")) {
                        target = new WildcardTarget(className, false);
                    } else if (member.equals("*()")) {
                        target = new WildcardTarget(className, true);
                    } else if (member.contains("(")) {
                        String name = member.substring(0, member.indexOf('('));
                        // For some reason old forge ATs let you use descriptors with dots instead of slashes
                        // We replicate this behavior here
                        String desc = member.substring(member.indexOf('('))
                                .replace('.', '/');
                        Type methodDescriptor = validateMethodDescriptor(desc, reader.getLineNumber());
                        if (!name.equals("<init>")) {
                            validateIdentifier(name, "method", reader.getLineNumber());
                        }
                        target = new MethodTarget(className, name, methodDescriptor);
                    } else {
                        validateIdentifier(member, "field", reader.getLineNumber());
                        target = new FieldTarget(className, member);
                    }
                }
                accessTransformers.add(new AccessTransformer(target, modifier, finalState, originName, reader.getLineNumber()));
            }
            return accessTransformers;
        }
    }

    private static AccessTransformer.Modifier parseModifier(String modifier, int line) {
        return switch (modifier) {
            case "public" -> AccessTransformer.Modifier.PUBLIC;
            case "private" -> AccessTransformer.Modifier.PRIVATE;
            case "protected" -> AccessTransformer.Modifier.PROTECTED;
            case "default" -> AccessTransformer.Modifier.DEFAULT;
            default -> throw new RuntimeException("Invalid modifier: " + modifier + " at line " + line);
        };
    }

    private static void locateInnerClassAts(String className, AccessTransformer.Modifier modifier, AccessTransformer.FinalState finalState, String originName, int index, List<AccessTransformer> ats) {
        int split = className.lastIndexOf('$');
        if (split == -1) {
            return;
        }
        String outer = className.substring(0, split);
        String inner = className.substring(split + 1);
        ats.add(new AccessTransformer(new InnerClassTarget(outer, inner), modifier, finalState, originName, index));
    }

    private static Type validateMethodDescriptor(String desc, int index) {
        Type methodDescriptor;
        try {
            methodDescriptor = Type.getType(desc);
        } catch (RuntimeException e) {
            throw new RuntimeException("Invalid method descriptor '"+ desc +"' at line " + index);
        }
        if (methodDescriptor.getSort() != Type.METHOD) {
            throw new RuntimeException("Invalid method descriptor '"+ desc +"' at line " + index);
        }
        return methodDescriptor;
    }

    private static void validateIdentifier(String name, String sort, int index) {
        if (!Character.isJavaIdentifierStart(name.charAt(0)) ||
                name.chars().skip(1).anyMatch(c -> !Character.isJavaIdentifierPart(c))
        ) {
            throw new RuntimeException("Invalid " + sort + "name '" + name + "' at line " + index);
        }
    }
}
