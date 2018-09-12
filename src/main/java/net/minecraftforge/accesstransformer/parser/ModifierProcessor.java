package net.minecraftforge.accesstransformer.parser;

import java.util.*;

import net.minecraftforge.accesstransformer.*;

public final class ModifierProcessor {
    private ModifierProcessor() {}
    public static AccessTransformer.Modifier modifier(String modifierString) {
        final String modifier = modifierString.toUpperCase(Locale.ROOT);
        final String ending = modifier.substring(modifier.length()-2, modifier.length());
        if ("+F".equals(ending) || "-F".equals(ending)) {
            return AccessTransformer.Modifier.valueOf(modifier.substring(0, modifier.length()-2));
        } else {
            return AccessTransformer.Modifier.valueOf(modifier);
        }
    }

    public static AccessTransformer.FinalState finalState(String modifierString) {
        final String modifier = modifierString.toUpperCase(Locale.ROOT);
        final String ending = modifier.substring(modifier.length()-2, modifier.length());
        if ("+F".equals(ending)) {
            return AccessTransformer.FinalState.MAKEFINAL;
        } else if ("-F".equals(ending)) {
            return AccessTransformer.FinalState.REMOVEFINAL;
        } else {
            return AccessTransformer.FinalState.LEAVE;
        }
    }
}
