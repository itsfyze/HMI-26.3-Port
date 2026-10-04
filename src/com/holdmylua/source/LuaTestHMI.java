package com.holdmylua.source;

import com.holdmylua.source.global.GlobalsStorage;
import com.holdmylua.source.lua_runtime.resource_controller.LuaAnimationResourceLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LuaTestHMI implements ClientModInitializer {
   public static final String MOD_ID = "holdmyitems";
   public static Matrix4f matricesMain = new Matrix4f();
   public static Matrix4f matricesOff = new Matrix4f();
   public static float tickProgress = 0.0F;
   public static final Logger LOGGER = LoggerFactory.getLogger("holdmyitems");
   public static Minecraft client = Minecraft.getInstance();
   public static float prevTime = 0.0F;
   public static float deltaTime = 0.0F;

   public void onInitializeClient() {
      ClassLoader parentClassLoader = this.getClass().getClassLoader();
      System.out.println("Using parent class loader: " + parentClassLoader);
      ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new LuaAnimationResourceLoader());
      Identifier packIdentifier = Identifier.fromNamespaceAndPath("holdmyitems", "pack_test");
      FabricLoader.getInstance().getModContainer("holdmyitems").ifPresent(container -> {
         ResourceManagerHelper.registerBuiltinResourcePack(
            packIdentifier, container, Component.literal("Example Pack!"), ResourcePackActivationType.DEFAULT_ENABLED
         );
         LOGGER.info("Registered embedded resource pack: {}", packIdentifier);
      });
      LOGGER.info("What you staring at?");
   }
}
