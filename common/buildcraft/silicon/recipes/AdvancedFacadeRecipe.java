/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.recipes;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.oredict.OreDictionary;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.JavaTools;
import buildcraft.api.recipes.CraftingResult;
import buildcraft.api.recipes.IIntegrationRecipeFactory;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.recipes.FlexibleRecipe;
import buildcraft.silicon.ItemRedstoneChipset;
import buildcraft.silicon.TileIntegrationTable;
import buildcraft.transport.ItemFacade;
import buildcraft.transport.ItemPipeWire;

public class AdvancedFacadeRecipe extends FlexibleRecipe implements IIntegrationRecipeFactory {
	public AdvancedFacadeRecipe(String id) {
		setContents(id, new ItemFacade(), 5000,
				new ItemStack(BuildCraftTransport.pipeWire, 1, OreDictionary.WILDCARD_VALUE),
				ItemRedstoneChipset.Chipset.RED.getStack());
	}

	@Override
	public boolean isValidInputA(ItemStack inputA) {
		return inputA != null && inputA.getItem() instanceof ItemFacade;
	}

	@Override
	public boolean isValidInputB(ItemStack inputB) {
		return inputB != null && (inputB.getItem() instanceof ItemFacade && ItemFacade.getType(inputB) == ItemFacade.FacadeType.Basic ||
				inputB.getItem() == BuildCraftTransport.plugItem);
	}

	@Override
	public CraftingResult craft(IInventory items, IFluidHandler fluids) {
		ItemStack inputA = items.decrStackSize(TileIntegrationTable.SLOT_INPUT_A, 1);
		ItemStack inputB = items.decrStackSize(TileIntegrationTable.SLOT_INPUT_B, 1);

		if (inputA == null || inputB == null) {
			return null;
		}

		CraftingResult result = super.craft(items, fluids);

		if (result == null) {
			return null;
		}

		PipeWire wire = null;

		for (ItemStack stack : result.usedItems) {
			if (stack != null && stack.getItem() instanceof ItemPipeWire) {
				wire = PipeWire.fromOrdinal(stack.getItemDamage());
				break;
			}
		}

		if (wire != null) {
			ItemFacade.FacadeState[] states = ItemFacade.getFacadeStates(inputA);
			ItemFacade.FacadeState additionalState;

			if (inputB.getItem() == BuildCraftTransport.plugItem) {
				additionalState = ItemFacade.FacadeState.createTransparent(wire);
			} else {
				additionalState = ItemFacade.getFacadeStates(inputB)[0];
				additionalState = ItemFacade.FacadeState.create(additionalState.block, additionalState.metadata, wire);
			}

			// if in states array exists state with the same wire just override it
			for (int i = 0; i < states.length; i++) {
				if (states[i].wire == wire) {
					states[i] = additionalState;

					result.energyCost = 2000;
					result.crafted = ItemFacade.getFacade(states);

					return result;
				}
			}

			result.energyCost = 5000;
			result.crafted = ItemFacade.getFacade(JavaTools.concat(states,
					new ItemFacade.FacadeState[] {additionalState}));

			return result;
		} else {
			return null;
		}
	}
}