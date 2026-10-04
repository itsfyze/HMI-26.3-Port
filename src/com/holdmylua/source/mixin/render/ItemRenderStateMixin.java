package com.holdmylua.source.mixin.render;

import com.holdmylua.source.global.DispatcherStorage;
import com.holdmylua.source.global.item_model.ItemModelStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.SubmitNodeStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FeatureRenderDispatcher.class)
public class ItemRenderStateMixin {
   @Inject(method = "prepareFrame", at = @At("RETURN"))
   private void clearRenderedItems(SubmitNodeStorage storage, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<?> cir) {
      DispatcherStorage.clear();
      ItemModelStorage.clear();
   }
}
