/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.fablabsmc.fablabs.api.provider.v1;

import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import io.github.fablabsmc.fablabs.impl.provider.ItemApiProviderAccessImpl;

/**
 * See {link ApiProviderAccess}. This subclass is for {@code ItemStack} game objects.
 */
public interface ItemApiProviderAccess<P extends ApiProvider<P, A>, A> extends ApiProviderAccess<P, A> {
	/**
	 * Causes the given items to to supply API provider instances by application of
	 * the given mapping function,
	 *
	 * <p>The mapping function should return {@link #absentApi()} if no component is available.
	 *
	 * @param mapping function that derives a provider instance from an item stack
	 * @param items one or more types for which the mapping will apply
	 */
	void registerProviderForItem(Function<ItemStack, P> mapping, ItemConvertible... items);

	/**
	 * Retrieves an {@code ApiProvider} used to obtain an API instance if present.
	 *
	 * @return a {@code ApiProvider} used to obtain an API instance if present.
	 * Will be {@link #absentProvider()} if no API is present.
	 */
	P getProviderFromStack(ItemStack stack);

	static <P extends ApiProvider<P, A>, A> ItemApiProviderAccess<P, A> registerAccess(Identifier id, Class<A> apiType, P absentProvider) {
		return ItemApiProviderAccessImpl.registerAccess(id, apiType, absentProvider);
	}

	@Nullable
	static ItemApiProviderAccess<?, ?> getAccess(Identifier id) {
		return ItemApiProviderAccessImpl.getAccess(id);
	}
}