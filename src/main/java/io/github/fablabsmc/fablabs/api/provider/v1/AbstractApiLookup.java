package io.github.fablabsmc.fablabs.api.provider.v1;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

/**
 * The building block for creating your own lookup class. You should extend this and only use `get` and `putIfAbsent`.
 */
public abstract class AbstractApiLookup<T, C, K, P> implements ApiLookup<T, C> {
    private static final Logger LOGGER = LogManager.getLogger();

    private final ApiKey<T> apiKey;
    private final ContextKey<C> contextKey;
    private volatile Map<K, P> lookups = new Reference2ReferenceOpenHashMap<>();

    protected AbstractApiLookup(ApiKey<T> apiKey, ContextKey<C> contextKey) {
        this.apiKey = apiKey;
        this.contextKey = contextKey;
    }

    protected @Nullable P get(K key) {
        return lookups.get(key);
    }

    protected synchronized void putIfAbsent(K key, P provider) {
        Objects.requireNonNull(key, "encountered null key while registering an ApiLookup mapping");
        Objects.requireNonNull(provider, "encountered null provider while registering an ApiLookup mapping");
        // We use a copy-on-write strategy to allow any number of reads to concur with a write
        Map<K, P> lookupsCopy = new Reference2ReferenceOpenHashMap<>(lookups);
        if(lookupsCopy.putIfAbsent(key, provider) != null) {
            LOGGER.warn("Attempted to overwrite a provider in an ApiLookup!");
            LOGGER.warn(" ApiKey: {} with identifier \"{}\"", getApiKey().getApiClass().getName(), getApiKey().getIdentifier().toString());
            LOGGER.warn(" ContextKey: {} with identifier \"{}\"", getContextKey().getContextClass().getName(), getContextKey().getIdentifier().toString());
            LOGGER.warn(" Key: {}", key.toString());
            LOGGER.warn(" Registered provider class: {}", lookupsCopy.get(key).getClass().getName());
            LOGGER.warn(" Offending provider class: {}", provider.getClass().getName());
        }
        lookups = lookupsCopy;
    }

    @NotNull
    public ApiKey<T> getApiKey() {
        return apiKey;
    }
    @NotNull
    public ContextKey<C> getContextKey() {
        return contextKey;
    }
}
