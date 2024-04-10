package net.neoforged.accesstransformer.parser;

import net.neoforged.accesstransformer.*;
import org.objectweb.asm.Type;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class AtParser {
    private AtParser() {}

    private static AccessTransformer.Modifier parseModifier(String modifier, int line) {
        return switch (modifier) {
            case "public" -> AccessTransformer.Modifier.PUBLIC;
            case "private" -> AccessTransformer.Modifier.PRIVATE;
            case "protected" -> AccessTransformer.Modifier.PROTECTED;
            case "default" -> AccessTransformer.Modifier.DEFAULT;
            default -> throw new RuntimeException("Invalid modifier: " + modifier + " at line " + line);
        };
    }

    public static List<AccessTransformer> parse(BufferedReader reader, String originName) throws IOException {
        List<AccessTransformer> accessTransformers = new ArrayList<>();
        String line;
        int index = 0;
        while ((line = reader.readLine()) != null) {
            index++;
            StringBuilder builder = new StringBuilder();
            line.chars().takeWhile(c -> c != '#').forEach(builder::appendCodePoint);
            String withoutComments = builder.toString();
            List<String> parts = new ArrayList<>();
            builder.setLength(0);
            for (char c : withoutComments.toCharArray()) {
                if (Character.isWhitespace(c)) {
                    if (!builder.isEmpty()) {
                        parts.add(builder.toString());
                    }
                    builder.setLength(0);
                } else {
                    builder.appendCodePoint(c);
                }
            }
            if (!builder.isEmpty()) {
                parts.add(builder.toString());
            }
            if (parts.isEmpty()) {
                continue;
            }
            if (parts.size() < 2) {
                throw new RuntimeException("Invalid line " + index + "; should be '<modifier> <class name>'");
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
            AccessTransformer.Modifier modifier = parseModifier(modifierString, index);
            String className = parts.get(1);
            int finalIndex = index;
            if (className.chars().reduce(0, (last, current) -> {
                if (last == 0 && !Character.isJavaIdentifierStart(current) ||
                        last != 0 && current != '.' && !Character.isJavaIdentifierPart(current)
                ) {
                    throw new RuntimeException("Invalid class name '"+className+"' at line " + finalIndex);
                }
                return current == '.' ? 0 : 1;
            }) != 1) {
                throw new RuntimeException("Invalid class name '"+className+"' at line " + index);
            }

            Target<?> target;

            if (parts.size() < 3) {
                target = new ClassTarget(className);
                locateInnerClassAts(className, modifier, finalState, originName, index, accessTransformers);
            } else {
                String identifier = parts.get(2);
                if (identifier.equals("*")) {
                    target = new WildcardTarget(className, false);
                } else if (identifier.equals("*()")) {
                    target = new WildcardTarget(className, true);
                } else if (identifier.contains("(")) {
                    String name = identifier.substring(0, identifier.indexOf('('));
                    // For some reason old forge ATs let you use descriptors with dots instead of slashes
                    // We replicate this behavior here
                    String desc = identifier.substring(identifier.indexOf('('))
                            .replace('.', '/');
                    Type methodDescriptor = validateMethodDescriptor(desc, index);
                    if (!name.equals("<init>")) {
                        validateIdentifier(name, "Invalid method name '", index);
                    }
                    target = new MethodTarget(className, name, methodDescriptor);
                } else {
                    validateIdentifier(identifier, "Invalid field name '", index);
                    target = new FieldTarget(className, identifier);
                }
            }
            accessTransformers.add(new AccessTransformer(target, modifier, finalState, originName, index));
        }
        return accessTransformers;
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

    private static void validateIdentifier(String name, String x, int index) {
        if (!Character.isJavaIdentifierStart(name.charAt(0)) ||
                name.chars().skip(1).anyMatch(c -> !Character.isJavaIdentifierPart(c))
        ) {
            throw new RuntimeException(x + name + "' at line " + index);
        }
    }
}
