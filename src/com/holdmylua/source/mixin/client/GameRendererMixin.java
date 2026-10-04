package com.holdmylua.source.mixin.client;

import com.holdmylua.source.LuaTestHMI;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    private static final long HMI_START_NANOS = System.nanoTime();
   @Inject(method = "render", at = @At("HEAD"))
   private void deltaTime(CallbackInfo ci) {
      float currentTime = (float)((System.nanoTime() - HMI_START_NANOS) / 1.0E9);
      LuaTestHMI.deltaTime = currentTime - LuaTestHMI.prevTime;
      LuaTestHMI.prevTime = currentTime;
      if (Minecraft.getInstance().isPaused()) {
         LuaTestHMI.deltaTime = 0.0F;
      } else {
         LuaTestHMI.deltaTime = (float)Math.min(0.05, LuaTestHMI.deltaTime);
      }
   }
}
