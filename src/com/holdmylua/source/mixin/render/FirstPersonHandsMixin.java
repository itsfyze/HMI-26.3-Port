package com.holdmylua.source.mixin.render;

import com.holdmylua.source.LuaTestHMI;
import com.holdmylua.source.access.LivingEntityAccessor;
import com.holdmylua.source.global.GlobalsStorage;
import com.holdmylua.source.lua_runtime.LuaScriptCache;
import com.holdmylua.source.lua_runtime.ScriptHolder;
import com.holdmylua.source.patricles.Particle;
import com.holdmylua.source.patricles.ParticleRenderManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class FirstPersonHandsMixin {
   @Unique private Item previousMainHand = Items.AIR;
   @Unique private Item previousOffHand = Items.AIR;
   @Unique private boolean mainHandSwitchEvent;
   @Unique private boolean offHandSwitchEvent;
   @Unique private ArrayList<Particle> particles;
   @Unique private ItemStackRenderState mainItemState;
   @Unique private ItemStackRenderState offItemState;
   @Unique private BlockModelRenderState mainBlockState;
   @Unique private BlockModelRenderState offBlockState;
   @Unique private BlockModelResolver blockModelResolver;
   @Unique private static final BlockDisplayContext BLOCK_CONTEXT = BlockDisplayContext.create();

   @Shadow
   protected abstract void renderPlayerArm(PoseStack matrices, SubmitNodeCollector collector,
      int light, float equipProgress, float swingProgress, HumanoidArm arm, PlayerRenderState playerState);

   @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
   private void holdmyitems$renderHands(
      PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState handState,
      float tickProgress, float pitch, InteractionHand hand, float swingProgress,
      ItemStack item, float equipProgress, PoseStack matrices, SubmitNodeCollector collector,
      int light, CallbackInfo ci
   ) {
      var player = Minecraft.getInstance().player;
      // Maps have their own two-handed submission path. Preserve its render-state handling.
      if (player == null || handState.isScoping || playerState.avatarRenderState == null
         || item.has(DataComponents.MAP_ID)) {
         return;
      }
      if (particles == null) {
         particles = new ArrayList<>();
         mainItemState = new ItemStackRenderState();
         offItemState = new ItemStackRenderState();
         mainBlockState = new BlockModelRenderState();
         offBlockState = new BlockModelRenderState();
         previousMainHand = Items.AIR;
         previousOffHand = Items.AIR;
      }
      boolean mainHand = hand == InteractionHand.MAIN_HAND;
      HumanoidArm arm = mainHand ? player.getMainArm() : player.getMainArm().getOpposite();
      boolean rightArm = arm == HumanoidArm.RIGHT;
      int side = rightArm ? 1 : -1;
      LivingEntityAccessor accessor = (LivingEntityAccessor)(Object)player;
      float mainSwing = accessor.hMI5_0$getMainHandSwingProgress(tickProgress);
      float offSwing = accessor.hMI5_0$getOffHandSwingProgress(tickProgress);
      swingProgress = mainHand ? mainSwing : offSwing;
      if (mainHand) {
         mainHandSwitchEvent = item.getItem() != previousMainHand;
         previousMainHand = item.getItem();
      } else {
         offHandSwitchEvent = item.getItem() != previousOffHand;
         previousOffHand = item.getItem();
      }
      GlobalsStorage.mainHandItem = handState.mainHandItem;
      GlobalsStorage.offHandItem = handState.offHandItem;
      GlobalsStorage.renderedStack = item;
      LuaTestHMI.tickProgress = tickProgress;

      matrices.pushPose();
      try {
         // Scene animation is shared, but arm and item poses are independent copies.
         execute(ScriptHolder.handScriptCache, ScriptHolder.handAddonsCache, matrices,
            rightArm, swingProgress, item, player, hand, mainHand, equipProgress, mainSwing, offSwing, accessor);
         if (!item.isEmpty()) {
            matrices.translate(0.0, -0.35, 0.2);
         }
         matrices.pushPose();
         try {
            execute(ScriptHolder.handRelativeScriptCache, ScriptHolder.handRelativeAddonsCache, matrices,
               rightArm, swingProgress, item, player, hand, mainHand, equipProgress, mainSwing, offSwing, accessor);
            if (!item.isEmpty()) {
               matrices.translate(1.5 * side, -0.3, -0.6);
               matrices.rotateAround(Axis.XP.rotationDegrees(15), 0.5F * side, 0.5F, 0.5F);
               matrices.rotateAround(Axis.YP.rotationDegrees(35 * side), 0.5F * side, 0.5F, 0.5F);
               matrices.rotateAround(Axis.ZP.rotationDegrees(-65 * side), 0.5F * side, 0.5F, 0.5F);
               matrices.scale(0.9F, 0.9F, 0.9F);
            }
            if (!playerState.avatarRenderState.isInvisible) {
               renderPlayerArm(matrices, collector, light, 0.0F, 0.0F, arm, playerState);
            }
         } finally {
            matrices.popPose();
         }
         if (!item.isEmpty()) {
            matrices.pushPose();
            try {
               boolean renderBlock = holdmyitems$renderAsBlock(item);
               matrices.translate(0.5 * side, -0.15, -0.85);
               matrices.rotateAround(Axis.XP.rotationDegrees(15), 0.5F, 0.5F, 0.5F);
               matrices.scale(0.9F, 0.9F, 0.9F);
               execute(ScriptHolder.itemScriptCache, ScriptHolder.itemAddonsCache, matrices,
                  rightArm, renderBlock ? 0.0F : swingProgress, item, player, hand, mainHand, equipProgress, mainSwing, offSwing, accessor);
               if (renderBlock) {
                  holdmyitems$submitBlock(item, matrices, collector, light, player, mainHand, side);
               } else {
               // HMI's poses are authored around third-person grip transforms, even in first person.
               // Resolve a fresh state rather than using vanilla's extracted first-person/blocking state.
               ItemStackRenderState itemState = mainHand ? mainItemState : offItemState;
               ItemStack renderStack = item.getUseAnimation() == net.minecraft.world.item.ItemUseAnimation.BLOCK
                  ? item.copy() : item;
               Minecraft.getInstance().getItemModelResolver().updateForLiving(itemState, renderStack,
                  rightArm ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                  player);
               itemState.submit(matrices, collector, light, OverlayTexture.NO_OVERLAY, 0);
               }
               (rightArm ? LuaTestHMI.matricesMain : LuaTestHMI.matricesOff).set(matrices.last().pose());
               ParticleRenderManager.draw(particles, matrices, collector, "ITEM", hand, light, player, tickProgress);
            } finally {
               matrices.popPose();
            }
         }
      } finally {
         matrices.popPose();
      }
      ParticleRenderManager.draw(particles, matrices, collector, "SCREEN", hand, light, player, tickProgress);
      ci.cancel();
   }

   @Unique
   private boolean holdmyitems$renderAsBlock(ItemStack item) {
      var block = Block.byItem(item.getItem());
      var state = block.defaultBlockState();
      return block != Blocks.AIR && !state.is(BlockTags.INSIDE_STEP_SOUND_BLOCKS)
         && !state.is(BlockTags.CROPS) && item.getUseAnimation() != ItemUseAnimation.EAT
         && !item.is(Items.REDSTONE) && !item.is(ItemTags.BANNERS) && !item.is(ItemTags.SKULLS)
         && GlobalsStorage.renderAsBlock.getOrDefault(item.getItem().toString(), true);
   }

   @Unique
   private void holdmyitems$submitBlock(ItemStack item, PoseStack matrices, SubmitNodeCollector collector,
      int light, AbstractClientPlayer player, boolean mainHand, int side
   ) {
      var client = Minecraft.getInstance();
      var block = Block.byItem(item.getItem());
      var state = block.defaultBlockState();
      if ((item.is(Items.LEVER) || state.is(BlockTags.BUTTONS)) && state.hasProperty(BlockStateProperties.ATTACH_FACE)) {
         state = state.setValue(BlockStateProperties.ATTACH_FACE, AttachFace.FLOOR);
      }
      // Match the original HMI world-block origin and size, bypassing item display rotations.
      matrices.translate(0.22 * side, 0.25, 0.2);
      if (!item.getItem().getDescriptionId().toLowerCase(java.util.Locale.ROOT).contains("torch")
         && !(block instanceof LanternBlock) && !state.is(BlockTags.ALL_HANGING_SIGNS)) {
         matrices.translate(-0.25 * side, -0.05, 0.0);
      } else {
         matrices.translate(-0.05 * side, 0.0, 0.0);
         matrices.scale(1.75F, 1.75F, 1.75F);
      }
      if (side < 0) matrices.translate(-0.3, 0.0, 0.0);
      matrices.scale(0.3F, 0.3F, 0.3F);
      matrices.translate(-0.9 * side, -0.45, -0.7);
      if (blockModelResolver == null) blockModelResolver = new BlockModelResolver(client.getModelManager());
      BlockModelRenderState blockRenderState = mainHand ? mainBlockState : offBlockState;
      if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
         matrices.pushPose();
         try {
            matrices.translate(0.0, 1.0, 0.0);
            var upper = state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
            blockModelResolver.update(blockRenderState, upper, BLOCK_CONTEXT);
            for (var tint : client.getBlockColors().getTintSources(upper)) blockRenderState.tintLayers().add(tint.color(upper) | 0xFF000000);
            blockRenderState.submit(matrices, collector, light, OverlayTexture.NO_OVERLAY, 0);
         } finally {
            matrices.popPose();
         }
      }
      blockModelResolver.update(blockRenderState, state, BLOCK_CONTEXT);
      for (var tint : client.getBlockColors().getTintSources(state)) blockRenderState.tintLayers().add(tint.color(state) | 0xFF000000);
      blockRenderState.submit(matrices, collector, light, OverlayTexture.NO_OVERLAY, 0);
   }

   @Unique
   private void execute(LuaScriptCache script, ArrayList<LuaScriptCache> addons, PoseStack matrices,
      boolean rightArm, float swingProgress, ItemStack item, AbstractClientPlayer player,
      InteractionHand hand, boolean mainHand, float equipProgress, float mainSwing,
      float offSwing, LivingEntityAccessor accessor
   ) {
      script.execute(matrices, rightArm, GlobalsStorage.registry, swingProgress, item, player, hand,
         mainHand, LuaTestHMI.deltaTime, equipProgress, mainSwing, offSwing, mainHandSwitchEvent,
         offHandSwitchEvent, accessor.hMI5_0$getMHandEvent(), accessor.hMI5_0$getOHandEvent(),
         mainHand ? accessor.hMI5_0$getMInteract() : accessor.hMI5_0$getOInteract(),
         accessor.hMI5_0$getBlockBreak(), particles);
      for (LuaScriptCache addon : addons) {
         addon.execute(matrices, rightArm, GlobalsStorage.registry, swingProgress, item, player, hand,
            mainHand, LuaTestHMI.deltaTime, equipProgress, mainSwing, offSwing, mainHandSwitchEvent,
            offHandSwitchEvent, accessor.hMI5_0$getMHandEvent(), accessor.hMI5_0$getOHandEvent(),
            mainHand ? accessor.hMI5_0$getMInteract() : accessor.hMI5_0$getOInteract(),
            accessor.hMI5_0$getBlockBreak(), particles);
      }
   }
}

