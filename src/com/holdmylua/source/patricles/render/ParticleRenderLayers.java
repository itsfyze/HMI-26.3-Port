package com.holdmylua.source.patricles.render;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public final class ParticleRenderLayers {
   private ParticleRenderLayers() {
   }

   public static RenderType additiveParticle(Identifier texture) {
      return RenderTypes.fireScreenEffect(texture);
   }
}
