package net.neoforged.accesstransformer;

import net.neoforged.accesstransformer.api.AccessTransformer;
import net.neoforged.accesstransformer.api.AccessTransformerEngine;
import net.neoforged.accesstransformer.api.TargetType;
import net.neoforged.accesstransformer.parser.AccessTransformerList;
import org.antlr.v4.runtime.CharStream;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

public class AccessTransformerEngineImpl implements AccessTransformerEngine {
    private final AccessTransformerList masterList = new AccessTransformerList();

    @Override
    public boolean transform(ClassNode clazzNode, final Type classType) {
        // this should never happen but safety first
        if (!masterList.containsClassTarget(classType)) {
            return false;
        }
        // list of methods that may have changed from private visibility, and therefore will need INVOKE_SPECIAL changed to INVOKE_VIRTUAL
        final Set<String> privateChanged = new HashSet<>();
        final Map<TargetType, Map<String, AccessTransformer>> transformersForTarget = masterList.getTransformersForTarget(classType);
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


    @Override
    public void loadAT(CharStream charStream) {
        masterList.loadAT(charStream);
    }

    @Override
    public void loadATFromPath(Path path) throws IOException {
        masterList.loadFromPath(path);
    }

    @Override
    public void loadATFromResource(String resourceName) throws URISyntaxException, IOException {
        masterList.loadFromResource(resourceName);
    }

    @Override
    public Set<Type> getTargets() {
        return masterList.getTargets();
    }
}
