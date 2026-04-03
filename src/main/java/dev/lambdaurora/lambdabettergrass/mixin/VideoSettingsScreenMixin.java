/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.lambdaurora.lambdabettergrass.gui.LBGOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VideoSettingsScreen.class)
abstract class VideoSettingsScreenMixin extends OptionsSubScreen {
	@Unique
	private OptionInstance<?> lbg$option;

	public VideoSettingsScreenMixin(Screen parent, Options gameOptions, Component title) {
		super(parent, gameOptions, title);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void onConstruct(Screen parent, Minecraft client, Options options, CallbackInfo ci) {
		this.lbg$option = LBGOption.getOption(this);
	}

	@WrapOperation(
			method = "addOptions",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/screens/options/VideoSettingsScreen;qualityOptions(Lnet/minecraft/client/Options;)[Lnet/minecraft/client/OptionInstance;"
			)
	)
	private OptionInstance<?>[] addOptionButton(Options options, Operation<OptionInstance<?>[]> original) {
		var old = original.call(options);
		var replaced = new OptionInstance<?>[old.length + 1];
		System.arraycopy(old, 0, replaced, 0, old.length);
		replaced[replaced.length - 1] = this.lbg$option;
		return replaced;
	}
}
