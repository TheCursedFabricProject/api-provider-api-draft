package io.github.fablabsmc.fablabs.api.provider.v1;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;

/**
 * Unique reference to a type of context.
 */
public final class ContextKey<C> {
    // Map must be before NO_CONTEXT or else `of` will NPE.
    private static final Map<Class<?>, Map<Identifier, ContextKey<?>>> CONTEXT_KEYS = new HashMap<>();
    public static final ContextKey<@Nullable Void> NO_CONTEXT = create(Void.class, new Identifier("fabric:no_context"));
    private final Class<C> clazz;
    private final Identifier identifier;

    private ContextKey(Class<C> clazz, Identifier identifier) {
        this.clazz = clazz;
        this.identifier = identifier;
    }

    public Class<C> getContextClass() {
        return clazz;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public static synchronized <C> ContextKey<C> create(Class<C> clazz, Identifier identifier) {
        Objects.requireNonNull(clazz, "Class type cannot be null");
        Objects.requireNonNull(identifier, "Context key cannot be null");

        CONTEXT_KEYS.putIfAbsent(clazz, new HashMap<>());
        CONTEXT_KEYS.get(clazz).putIfAbsent(identifier, new ContextKey<>(clazz, identifier));

        //noinspection unchecked
        return (ContextKey<C>) CONTEXT_KEYS.get(clazz).get(identifier);
    }
}
