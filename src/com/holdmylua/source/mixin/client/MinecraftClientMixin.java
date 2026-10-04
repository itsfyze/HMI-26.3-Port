package com.holdmylua.source.mixin.client;

import com.holdmylua.source.access.LivingEntityAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
   @Shadow public LocalPlayer player;

   @Inject(method = "startAttack", at = @At("RETURN"))
   private void holdmyitems$resetAttackSwing(CallbackInfoReturnable<Boolean> cir) {
      if (cir.getReturnValueZ() && player instanceof LivingEntityAccessor accessor) {
         accessor.hMI5_0$resetMainHandSwing(false);
      }
   }
}