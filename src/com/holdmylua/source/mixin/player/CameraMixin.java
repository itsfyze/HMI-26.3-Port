package com.holdmylua.source.mixin.player;

import com.holdmylua.source.access.CameraAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin implements CameraAccessor {
   @Shadow @Final private Quaternionf rotation;
   @Shadow private float xRot;
   @Shadow private float yRot;
   @Shadow @Final private static Vector3fc FORWARDS;
   @Shadow @Final private static Vector3fc UP;
   @Shadow @Final private static Vector3fc LEFT;
   @Shadow @Final private Vector3f forwards;
   @Shadow @Final private Vector3f up;
   @Shadow @Final private Vector3f left;
   @Shadow protected abstract void setPosition(Vec3 position);

   @Unique private float pitchM;
   @Unique private float yawM;
   @Unique private float rollM;
   @Unique private float xM;
   @Unique private float yM;
   @Unique private float zM;

   @Override
   public void hMI5_0$applyRotation() {
      rotation.rotateX((float)Math.toRadians(pitchM));
      rotation.rotateY((float)Math.toRadians(yawM));
      rotation.rotateZ((float)Math.toRadians(rollM));
   }

   @Override
   public void hMI5_0$setRotationValues(float pitch, float yaw, float roll) {
      pitchM = pitch;
      yawM = yaw;
      rollM = roll;
   }

   @Override
   public void hMI5_0$setPosValues(float x, float y, float z) {
      xM = x;
      yM = y;
      zM = z;
   }

   @Inject(method = "setRotation", at = @At("RETURN"))
   private void holdmyitems$setRotation(float yaw, float pitch, CallbackInfo ci) {
      if (FabricLoader.getInstance().isModLoaded("do_a_barrel_roll")) {
         return;
      }

      xRot = pitch;
      yRot = yaw;
      rotation.rotationYXZ(
         (float)Math.PI - (yaw + yawM) * (float)(Math.PI / 180.0),
         (-pitch + pitchM) * (float)(Math.PI / 180.0),
         rollM * (float)(Math.PI / 180.0)
      );
      FORWARDS.rotate(rotation, forwards);
      UP.rotate(rotation, up);
      LEFT.rotate(rotation, left);
   }

   @Inject(method = "setPosition(DDD)V", at = @At("HEAD"), cancellable = true)
   private void holdmyitems$setPosition(double x, double y, double z, CallbackInfo ci) {
      if (FabricLoader.getInstance().isModLoaded("do_a_barrel_roll")) {
         return;
      }

      setPosition(new Vec3(x + xM, y + yM, z + zM));
      ci.cancel();
   }
}
