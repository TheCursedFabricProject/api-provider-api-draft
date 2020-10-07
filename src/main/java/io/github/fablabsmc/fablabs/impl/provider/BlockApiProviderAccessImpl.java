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

package io.github.fablabsmc.fablabs.impl.provider;

import java.util.Objects;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import io.github.fablabsmc.fablabs.api.provider.v1.ApiProvider;
import io.github.fablabsmc.fablabs.api.provider.v1.BlockApiProviderAccess;
import io.github.fablabsmc.fablabs.mixin.provider.BlockEntityTypeAccessor;

public final class BlockApiProviderAccessImpl<P extends ApiProvider<P, A>, A> extends AbstractApiProviderAccess<P, A> implements BlockApiProviderAccess<P, A> {
	private final Reference2ReferenceOpenHashMap<Block, BlockProviderFunction<P, A>> blockMappings = new Reference2ReferenceOpenHashMap<>(256, Hash.VERY_FAST_LOAD_FACTOR);
	private final Reference2ReferenceOpenHashMap<BlockEntityType<?>, BlockEntityProviderFunction<P, A>> blockEntityMappings = new Reference2ReferenceOpenHashMap<>(256, Hash.VERY_FAST_LOAD_FACTOR);

	private BlockApiProviderAccessImpl(Class<A> apiType, P absentProvider) {
		super(apiType, absentProvider);
	}

	@Override
	public void registerProviderForBlock(BlockProviderFunction<P, A> mapping, Block... blocks) {
		Objects.requireNonNull(mapping, "encountered API provider mapping");

		for (final Block b : blocks) {
			Objects.requireNonNull(b, "encountered null block in API provider mapping");

			if (blockMappings.putIfAbsent(b, mapping) != null) {
				LOGGER.warn("Encountered duplicate API Provider registration for block " + Registry.BLOCK.getId(b));
			}
		}
	}

	@Override
	public void registerProviderForBlockEntity(BlockEntityProviderFunction<P, A> mapping, BlockEntityType<?>... blockEntityTypes) {
		Objects.requireNonNull(mapping, "encountered API provider mapping");

		for (final BlockEntityType<?> bet : blockEntityTypes) {
			Objects.requireNonNull(bet, "encountered null block entity type in API provider mapping");

			if (blockEntityMappings.putIfAbsent(bet, mapping) == null) {
				// register provider access for associated blocks to route to BE provider when retrieved through block state
				final BlockProviderFunction<P, A> blockMapping = (world, pos, blockState) -> mapping.getProvider(world.getBlockEntity(pos));
				((BlockEntityTypeAccessor) bet).getBlocks().forEach(block -> registerProviderForBlock(blockMapping, block));
			} else {
				LOGGER.warn("Encountered duplicate API Provider registration for block entity type " + Registry.BLOCK_ENTITY_TYPE.getId(bet));
			}
		}
	}

	@Override
	public P getProviderFromBlock(World world, BlockPos pos, BlockState blockState) {
		return blockMappings.get(blockState.getBlock()).getProvider(world, pos, blockState);
	}

	@Override
	public P getProviderFromBlockEntity(BlockEntity blockEntity) {
		return blockEntityMappings.get(blockEntity.getType()).getProvider(blockEntity);
	}

	private static final ApiProviderAccessRegistry<BlockApiProviderAccess<?, ?>> REGISTRY = new ApiProviderAccessRegistry<>();

	public static <P extends ApiProvider<P, A>, A> BlockApiProviderAccess<P, A> registerAccess(Identifier id, Class<A> apiType, P absentProvider) {
		final BlockApiProviderAccess<P, A> result = new BlockApiProviderAccessImpl<> (apiType, absentProvider);
		REGISTRY.register(id, result);
		return result;
	}

	@Nullable
	public static BlockApiProviderAccess<?, ?> getAccess(Identifier id) {
		return REGISTRY.get(id);
	}
}