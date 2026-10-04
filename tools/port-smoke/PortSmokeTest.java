package com.holdmylua.test;

import com.holdmylua.source.LuaTestHMI;
import com.holdmylua.source.access.CameraAccessor;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.Camera;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import org.joml.Matrix4f;

public class PortSmokeTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try {
        context.runOnClient(client -> {
            Camera camera = client.gameRenderer.mainCamera();
            var setRotation = Camera.class.getDeclaredMethod("setRotation", float.class, float.class);
            setRotation.setAccessible(true);
            setRotation.invoke(camera, 0.0F, 0.0F);
            Matrix4f before = camera.getViewRotationMatrix(new Matrix4f());
            setRotation.invoke(camera, 45.0F, 20.0F);
            Matrix4f after = camera.getViewRotationMatrix(new Matrix4f());
            if (before.equals(after, 0.0001F)) {
                throw new AssertionError("Camera view matrix stayed frozen after mouse rotation");
            }
            ((CameraAccessor)camera).hMI5_0$setRotationValues(5, 3, 2);
            setRotation.invoke(camera, 45.0F, 20.0F);
            Matrix4f offset = camera.getViewRotationMatrix(new Matrix4f());
            if (after.equals(offset, 0.0001F)) {
                throw new AssertionError("Camera script offsets did not update the cached matrix");
            }
            ((CameraAccessor)camera).hMI5_0$setRotationValues(0, 0, 0);
            LuaTestHMI.LOGGER.info("PORT_SMOKE_CAMERA_PASS");
        });
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
        try (var world = context.worldBuilder().create()) {
            context.waitTicks(40);
            context.runOnClient(client -> {
                client.player.setYRot(0);
                client.player.setXRot(0);
                client.player.setItemInHand(InteractionHand.MAIN_HAND, Items.AIR.getDefaultInstance());
                client.player.setItemInHand(InteractionHand.OFF_HAND, Items.AIR.getDefaultInstance());
            });
            context.waitTicks(15);
            context.takeScreenshot("port-empty-hands");
            context.runOnClient(client -> {
                client.player.setItemInHand(InteractionHand.MAIN_HAND, Items.DIAMOND_SWORD.getDefaultInstance());
                client.player.setItemInHand(InteractionHand.OFF_HAND, Items.SHIELD.getDefaultInstance());
            });
            context.waitTicks(20);
            context.takeScreenshot("port-sword-shield");
            context.runOnClient(client -> {
                client.player.setItemInHand(InteractionHand.MAIN_HAND, Items.APPLE.getDefaultInstance());
            });
            context.waitTicks(20);
            context.takeScreenshot("port-apple");
            java.util.concurrent.atomic.AtomicReference<java.util.concurrent.CompletableFuture<Void>> reload = new java.util.concurrent.atomic.AtomicReference<>();
            context.runOnClient(client -> {
                var repository = client.getResourcePackRepository();
                repository.reload();
                var packs = new java.util.ArrayList<>(repository.getSelectedIds());
                packs.add("file/Donut Pack (1).zip");
                packs.add("file/HoldMyItemsCustomV1.zip");
                repository.setSelected(packs);
                reload.set(client.reloadResourcePacks());
            });
            context.waitFor(client -> reload.get().isDone(), 1200);
            context.waitFor(client -> client.gui.screen() == null, 1200);
            context.runOnClient(client -> {
                client.player.setItemInHand(InteractionHand.MAIN_HAND, Items.DIAMOND_SWORD.getDefaultInstance());
                client.player.setItemInHand(InteractionHand.OFF_HAND, Items.SHIELD.getDefaultInstance());
            });
            context.waitTicks(60);
            context.takeScreenshot("port-custom-shield");
            context.runOnClient(client -> client.player.startUsingItem(InteractionHand.OFF_HAND));
            context.waitTicks(20);
            context.takeScreenshot("port-custom-shield-blocking");
            context.runOnClient(client -> client.player.stopUsingItem());
            context.runOnClient(client -> client.player.setItemInHand(InteractionHand.MAIN_HAND, Items.HAY_BLOCK.getDefaultInstance()));
            context.waitTicks(30);
            context.takeScreenshot("port-hay-block");
            context.runOnClient(client -> client.player.setItemInHand(InteractionHand.MAIN_HAND, Items.GRANITE.getDefaultInstance()));
            context.waitTicks(30);
            context.takeScreenshot("port-granite");
            context.runOnClient(client -> {
                client.player.setItemInHand(InteractionHand.MAIN_HAND, Items.SHIELD.getDefaultInstance());
                client.player.setItemInHand(InteractionHand.OFF_HAND, Items.HAY_BLOCK.getDefaultInstance());
            });
            context.waitTicks(30);
            context.takeScreenshot("port-offhand-hay-block");
            context.runOnClient(client -> {
                var repository = client.getResourcePackRepository();
                repository.reload();
                var packs = new java.util.ArrayList<>(repository.getSelectedIds());
                packs.add("file/Just 3D Stuffs - 26.3.zip");
                repository.setSelected(packs);
                reload.set(client.reloadResourcePacks());
            });
            context.waitFor(client -> reload.get().isDone(), 1200);
            context.waitFor(client -> client.gui.screen() == null, 1200);
            context.runOnClient(client -> {
                if (!client.getResourcePackRepository().getSelectedIds().contains("file/Just 3D Stuffs - 26.3.zip")) {
                    throw new AssertionError("Just 3D Stuffs was rejected during reload");
                }
                client.player.setItemInHand(InteractionHand.MAIN_HAND, Items.APPLE.getDefaultInstance());
                client.player.setItemInHand(InteractionHand.OFF_HAND, Items.NAME_TAG.getDefaultInstance());
            });
            context.waitTicks(60);
            context.takeScreenshot("port-j3d-apple-name-tag");
            context.runOnClient(client -> client.player.setItemInHand(InteractionHand.MAIN_HAND, Items.LANTERN.getDefaultInstance()));
            context.waitTicks(30);
            context.takeScreenshot("port-j3d-lantern");
            LuaTestHMI.LOGGER.info("PORT_SMOKE_J3D_PACK_PASS");
            LuaTestHMI.LOGGER.info("PORT_SMOKE_RENDER_PASS");
        }
    }
}
