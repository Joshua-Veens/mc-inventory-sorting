package com.firstmod.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.*;
import java.util.Collections;
import java.util.Comparator;

@Mixin(HandledScreen.class)
public abstract class MixinHandledScreen extends Screen {
	HandledScreen self = (HandledScreen)(Object)this;

	protected MixinHandledScreen(Text title) {
		super(title);
	}

	private ArrayList<Integer> nonSortingIndexes = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8));

	@Inject(at = @At("HEAD"), method = "keyPressed")
	private void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> info) {
		// This code is injected into the start of MinecraftClient.run()V
		if ( keyCode == 90 ) {
			PlayerInventory inventory = client.player.getInventory();
			ArrayList<ItemStack> itemsToSort = new ArrayList<>();

			for (int i = 0; i < inventory.MAIN_SIZE; i++){
				if (!nonSortingIndexes.contains(i)) {
					ItemStack itemStack = inventory.main.get(i);
					// Check if the ItemStack is not air
					if (!itemStack.isEmpty()) {
						itemsToSort.add(itemStack);
						// Remove the ItemStack from the inventory
						inventory.removeStack(i);
					}
				}
			}

			// Define a custom comparator
			Comparator<ItemStack> comparator = new Comparator<ItemStack>() {
				@Override
				public int compare(ItemStack itemStack1, ItemStack itemStack2) {
					// Compare logic here
					// For example, compare based on item name
					return itemStack1.getName().getString().compareTo(itemStack2.getName().getString());
				}
			};

			Collections.sort(itemsToSort, comparator);

			// Insert the sorted items back into the inventory at non-sorting indexes
			int sortedIndex = 0;
			for (int i = 0; i < inventory.MAIN_SIZE; i++) {
				if (!nonSortingIndexes.contains(i)) {
					// Insert the sorted item at the non-sorting index
					if (sortedIndex < itemsToSort.size()) {
						inventory.insertStack(i, itemsToSort.get(sortedIndex));
						sortedIndex++;
					} else {
						// No more items to insert, break the loop
						break;
					}
				}
			}
			inventory.updateItems();
		}
	}
}