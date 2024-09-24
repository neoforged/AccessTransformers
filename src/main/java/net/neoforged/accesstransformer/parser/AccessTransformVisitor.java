package net.neoforged.accesstransformer.parser;

import net.neoforged.accesstransformer.generated.*;

import net.neoforged.accesstransformer.*;
import org.antlr.v4.runtime.*;

import java.util.*;
import java.util.stream.*;


public class AccessTransformVisitor extends AtParserBaseVisitor<Void> {
    private List<AccessTransformer> accessTransformers = new ArrayList<>();
    private final String origin;

    public AccessTransformVisitor(final String origin) {
        this.origin = origin;
    }

    @Override
    public Void visitEntry(final AtParser.EntryContext ctx) {
        if (ctx.line_value() == null) {
            String className = ctx.class_name().getText();
            String modifier = ctx.keyword().getText();
            Target<?> target = new ClassTarget(className);
            accessTransformers.add(new AccessTransformer(target, ModifierProcessor.modifier(modifier), ModifierProcessor.finalState(modifier), this.origin, ctx.getStart().getLine()));

            int idx = className.lastIndexOf('$'); //Java uses this to identify inner classes, Scala/others use it for synthetics. Either way we should be fine as it will skip over classes that don't exist.
            if (idx != -1) {
                String parent = className.substring(0, idx);
                accessTransformers.add(new AccessTransformer(new InnerClassTarget(parent, className), ModifierProcessor.modifier(modifier), ModifierProcessor.finalState(modifier), this.origin, ctx.getStart().getLine()));
            }
        }
        return super.visitEntry(ctx);
    }

    @Override
    public Void visitFunction(final AtParser.FunctionContext ctx) {
        final AtParser.EntryContext entry = (AtParser.EntryContext) ctx.getParent().getParent();
        String className = entry.class_name().getText();
        String modifier = entry.keyword().getText();
        String methodName = ctx.func_name().getText();
        List<String> args = ctx.argument().stream().map(RuleContext::getText).collect(Collectors.toList());
        String retVal = ctx.return_value().getText();
        Target<?> target = new MethodTarget(className, methodName, args, retVal);
        accessTransformers.add(new AccessTransformer(target, ModifierProcessor.modifier(modifier), ModifierProcessor.finalState(modifier), this.origin, ctx.getStart().getLine()));
        return super.visitFunction(ctx);
    }

    @Override
    public Void visitField_name(final AtParser.Field_nameContext ctx) {
        final AtParser.EntryContext entry = (AtParser.EntryContext) ctx.getParent().getParent();
        String className = entry.class_name().getText();
        String modifier = entry.keyword().getText();
        String fieldName = ctx.getText();
        Target<?> target = new FieldTarget(className, fieldName);
        accessTransformers.add(new AccessTransformer(target, ModifierProcessor.modifier(modifier), ModifierProcessor.finalState(modifier), this.origin, ctx.getStart().getLine()));
        return super.visitField_name(ctx);
    }

    @Override
    public Void visitWildcard_field(final AtParser.Wildcard_fieldContext ctx) {
        final AtParser.EntryContext entry = (AtParser.EntryContext) ctx.getParent().getParent();
        String className = entry.class_name().getText();
        String modifier = entry.keyword().getText();
        Target<?> target = new WildcardTarget(className, false);
        accessTransformers.add(new AccessTransformer(target, ModifierProcessor.modifier(modifier), ModifierProcessor.finalState(modifier), this.origin, ctx.getStart().getLine()));
        return super.visitWildcard_field(ctx);
    }

    @Override
    public Void visitWildcard_method(final AtParser.Wildcard_methodContext ctx) {
        final AtParser.EntryContext entry = (AtParser.EntryContext) ctx.getParent().getParent();
        String className = entry.class_name().getText();
        String modifier = entry.keyword().getText();
        Target<?> target = new WildcardTarget(className, true);
        accessTransformers.add(new AccessTransformer(target, ModifierProcessor.modifier(modifier), ModifierProcessor.finalState(modifier), this.origin, ctx.getStart().getLine()));
        return super.visitWildcard_method(ctx);
    }

    public List<AccessTransformer> getAccessTransformers() {
        return accessTransformers;
    }
}
