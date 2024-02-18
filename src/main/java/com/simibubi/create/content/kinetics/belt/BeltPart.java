package com.simibubi.create.content.kinetics.belt;

import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.util.StringRepresentable;

public enum BeltPart implements StringRepresentable {
	START_0, START_1, START_2, START_3,
	MIDDLE,
	END_0, END_1, END_2, END_3,
	PULLEY_0, PULLEY_1, PULLEY_2, PULLEY_3;

	public static boolean anyStart(BeltPart beltPart)
	{
		return beltPart == START_0 ||
			   beltPart == START_1 ||
			   beltPart == START_2 ||
			   beltPart == START_3;
	}
	public static boolean anyPulley(BeltPart beltPart)
	{
		return beltPart == PULLEY_0 ||
			   beltPart == PULLEY_1 ||
			   beltPart == PULLEY_2 ||
			   beltPart == PULLEY_3;
	}
	public static boolean anyEnd(BeltPart beltPart)
	{
		return beltPart == END_0 ||
			   beltPart == END_1 ||
			   beltPart == END_2 ||
			   beltPart == END_3;
	}
	public static BeltPart getEndFromStart(BeltPart part) {
		return switch (part)
		{
			case START_1 -> END_1;
			case START_2 -> END_2;
			case START_3 -> END_3;
			default -> END_0;
		};
	}
	public static BeltPart getStartFromEnd(BeltPart part) {
		return switch (part)
		{
			case END_1 -> START_1;
			case END_2 -> START_2;
			case END_3 -> START_3;
			default -> START_0;
		};
	}
	public static int getTier(BeltPart part) {
		return switch (part)
		{
			case START_1, PULLEY_1, END_1 -> 1;
			case START_2, PULLEY_2, END_2 -> 2;
			case START_3, PULLEY_3, END_3 -> 3;
			default -> 0;
		};
	}
	public static BeltPart getStart(int tier)
	{
		return switch (tier)
		{
			case 1 -> START_1;
			case 2 -> START_2;
			case 3 -> START_3;
			default -> START_0;
		};
	}
	public static BeltPart getEnd(int tier)
	{
		return switch (tier)
		{
			case 1 -> END_1;
			case 2 -> END_2;
			case 3 -> END_3;
			default -> END_0;
		};
	}

	public static BeltPart getPulley(int tier)
	{
		return switch (tier)
		{
			case 1 -> PULLEY_1;
			case 2 -> PULLEY_2;
			case 3 -> PULLEY_3;
			default -> PULLEY_0;
		};
	}

	public static BeltPart fromOrdinal(int part)
	{
		return switch (part)
		{
            case 1 -> MIDDLE;
			case 2 -> END_0;
			case 3 -> PULLEY_0;
			default -> START_0;
		};
	}

	@Override
	public String getSerializedName() {
		return Lang.asId(name());
	}
}
