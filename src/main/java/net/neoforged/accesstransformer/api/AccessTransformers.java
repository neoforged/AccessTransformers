package net.neoforged.accesstransformer.api;

import net.neoforged.accesstransformer.AccessTransformerEngine;

public interface AccessTransformers {
    AccessTransformers INSTANCE = AccessTransformerEngine.INSTANCE;

    void loadAT(final String accessTransformer, final String resourceName);
}
