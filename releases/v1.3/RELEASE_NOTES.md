# HMI 26.3 Port v1.3

Unofficial Hold My Items port for Minecraft 26.3. Original mod author: sapling.

## Installation

Install **HMI 26.3 v1.3 Port.jar** in your Fabric instance's `mods` folder alongside Fabric API for Minecraft 26.3. Requires Java 25 and Fabric Loader 0.19.5 or newer. Remove older HMI JARs before restarting.

The release JAR is the exact desktop copy prepared in the previous porting session. Its internal version is `5.1.1+26.3.port4`.

## Port changes

- Restored camera matrix updates and first-person arm rendering.
- Connected arm, held-item, and relative-hand Lua scripts to the renderer.
- Restored third-person hand model transforms for held items, including shield grip.
- Integrated the Minecraft 26.3 block-model submission API for held blocks.
- Included LuaJ and the built-in Example Pack.

## Validation and limitations

The previous porting session verified a successful build and in-game smoke checks for camera updates, empty hands, sword/shield, and apple rendering. Further testing is needed for attacking, eating, bows, maps, resource reloads, and combinations with other mods.

Model-part animation remains incomplete: `ModelPartAnimator.applyPoses` still needs integration with the 26.3 item-model rendering pipeline.

The `-sources.jar` and source ZIP are development downloads. Do not put them in your `mods` folder.
