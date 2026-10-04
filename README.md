# Hold My Items — Minecraft 26.3 Port

An unofficial Fabric port of **Hold My Items** for Minecraft **26.3**. Original mod author: **sapling**. This repository contains the port source, resources, build configuration, and an isolated rendering smoke test.

## Download and install

Download **HMI 26.3 v1.3 Port.jar** from [Releases](https://github.com/itsfyze/HMI-26.3-Port/releases). Its internal mod version is `5.1.1+26.3.port4`.

1. Install Fabric Loader **0.19.5 or newer** for Minecraft **26.3** and the matching Fabric API.
2. Place the release JAR in your instance's `mods` folder.
3. Remove older Hold My Items JARs from that instance to avoid duplicate mod IDs.
4. Restart Minecraft. The built-in Example Pack is included and enabled by default.

The source ZIP and `-sources.jar` are for development; install only the mod JAR. This is a client-side mod and requires Java **25**.

## Build and run

Requires JDK 25, Fabric Loader 0.19.5 or newer, and Fabric API for Minecraft 26.3.
Set `JAVA_HOME` to your JDK 25 directory. The Windows wrapper uses that directory even when Java on PATH is an older version.

Build from PowerShell in this directory:

```powershell
.\gradlew.bat --gradle-user-home "$env:USERPROFILE\.gradle" clean build
```

Install `build/libs/modid-5.1.1+26.3.port4.jar` in your Fabric instance's `mods` folder alongside Fabric API. Do not install the `-sources.jar`.
The mod includes LuaJ and the built-in Example Pack, enabled by default.

Launch the development client:

```powershell
.\gradlew.bat --gradle-user-home "$env:USERPROFILE\.gradle" runClient
```

The explicit cache path avoids a wrapper error when Java's user-home directory resolves to `C:\`.

## Port status

The camera hook preserves Minecraft 26.3's matrix-cache invalidation. Arm and item scripts run in independent rendering stages using the original 1.21.11 transforms.
Held items resolve third-person left/right hand model transforms, as the original HMI renderer does, instead of reusing vanilla's first-person states. This restores the shield grip and preserves resource-pack textures and model display settings.
Held blocks use Minecraft 26.3's block-model submission API with the original HMI world-block transforms and scale. This bypasses item display rotations; scripts can opt individual blocks back into item rendering through `renderAsBlock`.
An isolated client gametest checks camera rotation and script-offset matrix updates, creates a test world, and captures empty hands, sword/shield, and apple screenshots. These checks pass without Lua runtime errors.

Run this regression check with:

```powershell
.\gradlew.bat --gradle-user-home "$env:USERPROFILE\.gradle" --init-script tools/port-smoke.gradle runClient
```

The test source and entrypoint are enabled only by this init script. Run a normal `clean build` afterward to produce the release JAR.
The user's Modrinth mod combination, attacking, eating, bows, maps, and resource reloads still need further testing.
Model-part animation remains incomplete: `ModelPartAnimator.applyPoses` needs integration with the 26.3 item-model rendering pipeline.

## Repository layout

- `src/com/holdmylua/`: Java source for the renderer, Lua integration, and mixins.
- `src/assets/holdmyitems/`: mod assets.
- `src/resourcepacks/pack_test/`: bundled Example Pack and Lua animation scripts.
- `src/META-INF/jars/`: bundled LuaJ dependency.
- `tools/port-smoke/` and `tools/port-smoke.gradle`: optional in-game regression test.
- `gradle/` and `gradlew*`: Gradle wrapper.
- `.github/workflows/`: build verification and release publishing.
- `releases/v1.3/`: exact prebuilt release files and release notes.

Local caches, game instances, logs, decompiled reference files, and install backups are excluded from the source upload.

## Reporting problems

Open an [issue](https://github.com/itsfyze/HMI-26.3-Port/issues) with your Minecraft, Fabric Loader, Fabric API, and HMI versions; reproduction steps; relevant log excerpts; and screenshots for rendering problems. Include your resource packs and other mods when relevant. Remove personal information from logs before posting.

## Credits and license

Hold My Items credits **sapling** in its mod metadata. This is an unofficial port and is not an official Minecraft, Mojang, Microsoft, or Fabric project.

The repository retains the supplied [CC0 license](LICENSE) and original `src/LICENSE_holdmyitems`. LuaJ is bundled as a dependency and retains its own upstream licensing.
