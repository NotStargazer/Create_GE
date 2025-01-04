package com.simibubi.create.content.kinetics.fan.processing;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FanProcessing {
	public static boolean canProcess(ItemEntity entity, FanProcessingType type) {
		if (entity.getPersistentData()
			.contains("CreateData")) {
			CompoundTag compound = entity.getPersistentData()
				.getCompound("CreateData");
			if (compound.contains("Processing")) {
				CompoundTag processing = compound.getCompound("Processing");

				if (AllFanProcessingTypes.parseLegacy(processing.getString("Type")) != type)
					return type.canProcess(entity.getItem(), entity.level());
				else if (processing.getInt("Time") >= 0)
					return true;
				else if (processing.getInt("Time") == -1)
					return false;
			}
		}
		return type.canProcess(entity.getItem(), entity.level());
	}

	public static boolean applyProcessing(ItemEntity entity, FanProcessingType type) {
		if (decrementProcessingTime(entity, type) != 0)
			return false;
		ItemStack entityItem = entity.getItem();
		ItemStack processItem = entityItem.copy();
		processItem.setCount(1);
		List<ItemStack> stacks = type.process(processItem, entity.level());
		if (stacks == null)
			return false;
		if (stacks.isEmpty()) {
			if (entityItem.getCount() == 1)
			{
				entity.discard();
			}
			else
			{
				ItemStack newStack = entityItem.copy();
				newStack.setCount(newStack.getCount() - 1);
				entity.setItem(newStack);
				resetProcessingTime(entity, type);
			}
			return false;
		}
		if (entityItem.getCount() == 1)
		{
			entity.discard();
		}
		else
		{
			ItemStack newStack = entityItem.copy();
			newStack.setCount(newStack.getCount() - 1);
			entity.setItem(newStack);
			resetProcessingTime(entity, type);
		}
		for (ItemStack additional : stacks) {
			ItemEntity entityIn = new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), additional);
			entityIn.setDeltaMovement(entity.getDeltaMovement());
			entity.level().addFreshEntity(entityIn);
		}
		return true;
	}

	public static TransportedResult applyProcessing(TransportedItemStack transported, Level world, FanProcessingType type) {
		TransportedResult ignore = TransportedResult.doNothing();
		if (transported.processedBy != type) {
			transported.processedBy = type;
			int timeModifierForStackSize = ((transported.stack.getCount() - 1) / 16) + 1;
			int processingTime =
				(int) (AllConfigs.server().kinetics.fanProcessingTime.get() * timeModifierForStackSize) + 1;
			transported.processingTime = processingTime;
			if (!type.canProcess(transported.stack, world))
				transported.processingTime = -1;
			return ignore;
		}
		if (transported.processingTime == -1)
			return ignore;
		if (transported.processingTime-- > 0)
			return ignore;

		List<ItemStack> stacks = type.process(transported.stack, world);
		if (stacks == null)
			return ignore;

		List<TransportedItemStack> transportedStacks = new ArrayList<>();
		for (ItemStack additional : stacks) {
			TransportedItemStack newTransported = transported.getSimilar();
			newTransported.stack = additional.copy();
			transportedStacks.add(newTransported);
		}
		return TransportedResult.convertTo(transportedStacks);
	}

	private static void resetProcessingTime(ItemEntity entity, FanProcessingType type) {
		CompoundTag nbt = entity.getPersistentData();

		if (!nbt.contains("CreateData"))
			return;
		CompoundTag createData = nbt.getCompound("CreateData");

		if (!createData.contains("Processing"))
			return;
		CompoundTag processing = createData.getCompound("Processing");

		if (!processing.contains("Type") || AllFanProcessingTypes.parseLegacy(processing.getString("Type")) != type)
			return;

		int value = AllConfigs.server().kinetics.fanProcessingTime.get() + 1;
		processing.putInt("Time", value);
	}

	private static int decrementProcessingTime(ItemEntity entity, FanProcessingType type) {
		CompoundTag nbt = entity.getPersistentData();

		if (!nbt.contains("CreateData"))
			nbt.put("CreateData", new CompoundTag());
		CompoundTag createData = nbt.getCompound("CreateData");

		if (!createData.contains("Processing"))
			createData.put("Processing", new CompoundTag());
		CompoundTag processing = createData.getCompound("Processing");

		if (!processing.contains("Type") || AllFanProcessingTypes.parseLegacy(processing.getString("Type")) != type) {
			processing.putString("Type", FanProcessingTypeRegistry.getIdOrThrow(type).toString());
			int processingTime =
				(int) (AllConfigs.server().kinetics.fanProcessingTime.get()) + 1;
			processing.putInt("Time", processingTime);
		}

		int value = processing.getInt("Time") - 1;
		processing.putInt("Time", value);
		return value;
	}
}
