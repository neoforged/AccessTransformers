package net.neoforged.accesstransformer;

import net.neoforged.accesstransformer.api.AccessTransformers;
import net.neoforged.accesstransformer.parser.AccessTransformerList;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import net.neoforged.accesstransformer.parser.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public enum AccessTransformerEngine implements AccessTransformers {
    INSTANCE;

    private final AccessTransformerList masterList = new AccessTransformerList();

    public boolean transform(ClassNode clazzNode, final Type classType) {
        // this should never happen but safety first
        if (!masterList.containsClassTarget(classType)) {
            return false;
        }
        // list of methods that may have changed from private visibility, and therefore will need INVOKE_SPECIAL changed to INVOKE_VIRTUAL
        final Set<String> privateChanged = new HashSet<>();
        final Map<TargetType, Map<String,AccessTransformer>> transformersForTarget = masterList.getTransformersForTarget(classType);
        if (transformersForTarget.containsKey(TargetType.CLASS)) {
            // apply class transform and any wild cards
            transformersForTarget.get(TargetType.CLASS).forEach((n,at) -> at.applyModifier(clazzNode, ClassNode.class, privateChanged));
        }

        if (transformersForTarget.containsKey(TargetType.FIELD)) {
            final Map<String, AccessTransformer> fieldTransformers = transformersForTarget.get(TargetType.FIELD);
            clazzNode.fields.stream()
                    .filter(fn -> fieldTransformers.containsKey(fn.name))
                    .forEach(fn -> fieldTransformers.get(fn.name).applyModifier(fn, FieldNode.class, privateChanged));
        }
        if (transformersForTarget.containsKey(TargetType.METHOD)) {
            final Map<String, AccessTransformer> methodTransformers = transformersForTarget.get(TargetType.METHOD);
            clazzNode.methods.stream()
                    .filter(mn -> methodTransformers.containsKey(mn.name + mn.desc))
                    .forEach(mn -> methodTransformers.get(mn.name + mn.desc).applyModifier(mn, MethodNode.class, privateChanged));
        }
        if (!privateChanged.isEmpty()) {
            clazzNode.methods.forEach(mn ->
                StreamSupport.stream(Spliterators.spliteratorUnknownSize(mn.instructions.iterator(), Spliterator.ORDERED), false)
                    .filter(i -> i.getOpcode() == Opcodes.INVOKESPECIAL)
                    .map(MethodInsnNode.class::cast)
                    .filter(m -> privateChanged.contains(m.name + m.desc))
                    .forEach(m -> m.setOpcode(Opcodes.INVOKEVIRTUAL)));
        }
        return true;
    }

    public void addResource(final Path path, final String resourceName) {
        try {
            masterList.loadFromPath(path, resourceName);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid path "+ path, e);
        }
    }

    public boolean handlesClass(final Type className) {
        return masterList.containsClassTarget(className);
    }
}
