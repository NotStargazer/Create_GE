package com.simibubi.create.ge;

import com.simibubi.create.AllBlocks;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltPart;

import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlock;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

@MethodsReturnNonnullByDefault
public class CreateGrandExpanse
{
	public static boolean isInAnyOf(BlockEntry<?>[] tieredBlockCollection, ItemStack stack)
	{
		for (BlockEntry<?> entry : tieredBlockCollection) {
			if (entry.isIn(stack))
				return true;
		}

		return false;
	}

	public static boolean hasAnyOf(BlockEntry<?>[] tieredBlockCollection, BlockState state)
	{
		for (BlockEntry<?> entry : tieredBlockCollection) {
			if (entry.has(state))
				return true;
		}

		return false;
	}

	public static boolean isAnyOf(BlockEntry<?>[] tieredBlockCollection, Block blockIn)
	{
		for (BlockEntry<?> entry : tieredBlockCollection) {
			if (entry.is(blockIn))
				return true;
		}

		return false;
	}

	public static ItemStack asStackByBeltPart(BeltPart part)
	{
		return asStackByBeltPart(part, 1);
	}

	public static ItemStack asStackByBeltPart(BeltPart part, int count)
	{
		return switch (part)
		{
			case PULLEY_0, START_0, END_0 -> AllBlocks.SHAFTS[0].asStack(count);
			case PULLEY_1, START_1, END_1 -> AllBlocks.SHAFTS[1].asStack(count);
			case PULLEY_2, START_2, END_2 -> AllBlocks.SHAFTS[2].asStack(count);
			case PULLEY_3, START_3, END_3 -> AllBlocks.SHAFTS[3].asStack(count);
			default -> ItemStack.EMPTY;
		};
	}

	public static int getValueCount(Map<Integer, Integer> map)
	{
		int count = 0;
		for (Integer value : map.values())
		{
			count += value;
		}
		return count;
	}

	public static int getTier(Block block)
	{
		KineticBlock kineticBlock = (KineticBlock) block;
		return kineticBlock.tier;
	}

	public static BeltPart getPulleyTierFromShaft(ItemStack stack)
	{
		for (int tier = 0; tier < 4; tier++) {
			if (AllBlocks.SHAFTS[tier].isIn(stack))
				return switch (tier)
				{
					case 1 -> BeltPart.PULLEY_1;
					case 2 -> BeltPart.PULLEY_2;
					case 3 -> BeltPart.PULLEY_3;
					default -> BeltPart.PULLEY_0;
				};
		}

		return BeltPart.PULLEY_0;
	}
}
