package com.simibubi.create.content.schematics.packet;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltPart;
import com.simibubi.create.content.schematics.SchematicPrinter;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.ge.CreateGrandExpanse;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.network.NetworkEvent.Context;

public class SchematicPlacePacket extends SimplePacketBase {

	public ItemStack stack;

	public SchematicPlacePacket(ItemStack stack) {
		this.stack = stack;
	}

	public SchematicPlacePacket(FriendlyByteBuf buffer) {
		stack = buffer.readItem();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeItem(stack);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player == null)
				return;
			if (!player.isCreative())
				return;

			Level world = player.getLevel();
			SchematicPrinter printer = new SchematicPrinter();
			printer.loadSchematic(stack, world, !player.canUseGameMasterBlocks());
			if (!printer.isLoaded() || printer.isErrored())
				return;

			boolean includeAir = AllConfigs.server().schematics.creativePrintIncludesAir.get();

			while (printer.advanceCurrentPos()) {
				if (!printer.shouldPlaceCurrent(world))
					continue;

				printer.handleCurrentTarget((pos, state, blockEntity) -> {

					String stateName = state.getBlock().defaultBlockState().toString();
					BlockState newState = switch (stateName)
					{
						case "Block{create:shaft}" -> AllBlocks.SHAFTS[0].getDefaultState();
						case "Block{create:cogwheel}" -> AllBlocks.COGWHEELS[0].getDefaultState();
						case "Block{create:large_cogwheel}" -> AllBlocks.LARGE_COGWHEELS[0].getDefaultState();
						case "Block{create:gearbox}" -> AllBlocks.GEARBOXES[0].getDefaultState();
						case "Block{create:clutch}" -> AllBlocks.CLUTCHES[0].getDefaultState();
						case "Block{create:gearshift}" -> AllBlocks.GEARSHIFTS[0].getDefaultState();
						case "Block{create:encased_chain_drive}" -> AllBlocks.ENCASED_CHAIN_DRIVES[0].getDefaultState();
						case "Block{create:adjustable_chain_gearshift}" -> AllBlocks.ADJUSTABLE_CHAIN_GEARSHIFTS[0].getDefaultState();
                        default -> state;
                    };

					for (Property<?> property : state.getProperties()) {
						CreateGrandExpanse.copyProperty(state, newState, property);
					}

					if (stateName.contains("Block{create:belt}")) {
						int part = state.getValue(BeltBlock.PART).ordinal();
						newState.setValue(BeltBlock.PART, BeltPart.fromOrdinal(part));
					}

					boolean placingAir = newState.isAir();
					if (placingAir && !includeAir)
						return;

					CompoundTag data = blockEntity != null ? blockEntity.saveWithFullMetadata() : null;
					BlockHelper.placeSchematicBlock(world, newState, pos, null, data);
				}, (pos, entity) -> {
					world.addFreshEntity(entity);
				});
			}
		});
		return true;
	}
}
