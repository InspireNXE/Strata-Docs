# Minecraft

![Strata-Minecraft logo](../../assets/brand/strata-minecraft.svg){ .strata-logo }

The Minecraft adapter, Strata-Minecraft, draws Strata through vanilla's GUI pipeline and takes input from vanilla's screen callbacks. A Strata screen is shown in a vanilla `Screen`, uses vanilla's sprites, font and cursors, and follows the player's resource packs and GUI scale, so it looks and behaves like the screens around it. Screens are written against Strata's API exactly as for [Swing](../swing.md) or any other adapter, and the same screen class runs on both.

The adapter lives in the [Strata-Minecraft](https://github.com/InspireNXE/Strata-Minecraft) repository. Its `common` project is written against vanilla classes only, and each loader's jar compiles that code with a thin entry point and the loader's metadata.

## Versions and loaders

Each Minecraft version has a branch of its own: `main` for the newest, `mc/1.21.1` and `mc/1.20.1` for the versions most mods settled on.

| Minecraft | Branch | ![](../../assets/brand/loader-neoforge.svg){ .strata-icon } NeoForge | ![](../../assets/brand/loader-forge.svg){ .strata-icon } Forge | ![](../../assets/brand/loader-fabric.svg){ .strata-icon } Fabric | ![](../../assets/brand/loader-sponge.svg){ .strata-icon } Sponge |
|---|---|---|---|---|---|
| 26.3 | `main` | 26.3.0.83 or newer, SpongeNeo optional | 66.0.13 or newer, SpongeForge optional | Loader 0.19.5 or newer | SpongeVanilla for SpongeAPI 21 |
| 1.21.1 | `mc/1.21.1` | 21.1.264 or newer, SpongeNeo optional | 52.1.20 or newer, SpongeForge optional | Loader 0.19.5 or newer | Through SpongeNeo and SpongeForge, SpongeAPI 12 |
| 1.20.1 | `mc/1.20.1` | | 47.4.28 or newer | Loader 0.19.5 or newer | |

Fabric API is not needed at run time on any version. The pages in this section describe `main`, for Minecraft 26.3, which needs Java 25.

Strata is a client-side mod with the id `strata` on every loader, and a plugin with the id `strata` on SpongeVanilla. Players install the jar for their loader next to your mod.

## Artifacts

The jars for 26.3 are published to Maven Central in the group `org.inspirenxe`. Until 0.1.0 is released there are snapshots only, version `0.1.0-SNAPSHOT`, in Central's snapshot repository at `https://central.sonatype.com/repository/maven-snapshots/`.

| Artifact | What it is |
|---|---|
| `strata-minecraft-neoforge` | The NeoForge mod |
| `strata-minecraft-forge` | The Forge mod |
| `strata-minecraft-fabric` | The Fabric mod |
| `strata-minecraft-spongevanilla` | The SpongeVanilla plugin |
| `strata-minecraft-common` | The adapter's vanilla-only code, which every loader jar compiles in |
| `strata-minecraft-sponge` | The code that needs SpongeAPI, which the NeoForge, Forge and SpongeVanilla jars compile in |

A mod depends on the jar for its loader. `common` and `sponge` are not mods and are not meant to be depended on directly.

The `mc/1.21.1` and `mc/1.20.1` branches are not published yet. Their versions are `0.1.0+1.21.1-SNAPSHOT` and `0.1.0+1.20.1-SNAPSHOT`, and `./gradlew publishToMavenLocal` in a checkout of the branch puts them in your local Maven repository.

## Setup

Every loader takes the same repository:

```groovy
repositories {
    maven {
        url = uri('https://central.sonatype.com/repository/maven-snapshots/')
        mavenContent {
            snapshotsOnly()
        }
    }
}
```

The loader jar lists Strata's `api`, `spi` and `core` as run-time dependencies only, so add `strata-api` for compiling. Use only the `org.inspirenxe.strata.api` packages and the adapter's `org.inspirenxe.strata.minecraft` packages from your mod. The jar also carries `spi` and `core` because the game needs them at run time, but they are not part of the API and change without notice.

Then declare `strata` as a required client-side dependency in your mod's metadata, so the game refuses to start without it. The version ranges below start at `0.1.0-SNAPSHOT`, because the loaders sort a snapshot before its release and a range starting at `0.1.0` would reject the current build.

### NeoForge

In a ModDevGradle project:

```groovy
dependencies {
    implementation 'org.inspirenxe:strata-minecraft-neoforge:0.1.0-SNAPSHOT'
    compileOnly 'org.inspirenxe:strata-api:0.1.0-SNAPSHOT'
}
```

In `META-INF/neoforge.mods.toml`, with your own mod id in place of `examplemod`:

```toml
[[dependencies.examplemod]]
modId = "strata"
type = "required"
versionRange = "[0.1.0-SNAPSHOT,)"
ordering = "AFTER"
side = "CLIENT"
```

`ordering = "AFTER"` constructs your mod after Strata, so Strata has handed over its screen controller by the time your mod's constructor runs.

### Forge

In a ForgeGradle 7 project:

```groovy
dependencies {
    implementation 'org.inspirenxe:strata-minecraft-forge:0.1.0-SNAPSHOT'
    compileOnly 'org.inspirenxe:strata-api:0.1.0-SNAPSHOT'
}
```

In `META-INF/mods.toml`. Forge marks a required dependency with `mandatory` where NeoForge uses `type`:

```toml
[[dependencies.examplemod]]
modId = "strata"
mandatory = true
versionRange = "[0.1.0-SNAPSHOT,)"
ordering = "AFTER"
side = "CLIENT"
```

### Fabric

In a Loom project. Loom 1.18 does not remap, so use `implementation`, not `modImplementation`:

```groovy
dependencies {
    implementation 'org.inspirenxe:strata-minecraft-fabric:0.1.0-SNAPSHOT'
    compileOnly 'org.inspirenxe:strata-api:0.1.0-SNAPSHOT'
}
```

In `fabric.mod.json`:

```json
"depends": {
    "strata": "*"
}
```

### SpongeVanilla

In a plugin project that compiles against SpongeAPI 21. Take the Strata plugin as `compileOnly`: players install it next to yours, and SpongeVanilla loads each plugin jar as one module, so two plugin jars cannot hold the same packages.

```groovy
dependencies {
    compileOnly 'org.inspirenxe:strata-minecraft-spongevanilla:0.1.0-SNAPSHOT'
    compileOnly 'org.inspirenxe:strata-api:0.1.0-SNAPSHOT'
}
```

In `META-INF/sponge_plugins.json`, inside your plugin's entry:

```json
"dependencies": [
    {
        "id": "strata",
        "version": "[0.1.0-SNAPSHOT,)",
        "load-order": "after",
        "optional": false
    }
]
```

Strata starts on its own `ConstructPluginEvent`, which Sponge sends to plugins in dependency order. With `"load-order": "after"`, Strata has started by the time your plugin hears its `ConstructPluginEvent`.

### Client-only code

Strata runs on the client only. Its NeoForge mod is declared for the client dist, its Forge mod as `clientSideOnly = true`, its Fabric mod with `"environment": "client"`, and its SpongeVanilla plugin does nothing on a dedicated server. Keep every class that touches Strata out of code that runs on a dedicated server, as you would for vanilla screen classes.

## Your first screen

A screen is the same class it would be on any other adapter:

```java
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.text.Text;

public final class SettingsScreen implements Screen {
    @Override
    public Component<?> build(final ComponentFactory factory) {
        final Label status = factory.label("Ready");
        final Button save = factory.button("Save").onEvent(Button.PRESSED, event -> status.setText("Saved"));
        final StackPanel column = factory.stackPanel().setSpacing(4).setPadding(8).add(status).add(save);
        return factory.anchorPanel().add(column.setAnchor(Anchor.CENTER)).setAnchor(Anchor.FILL);
    }

    @Override
    public Text title() {
        return Text.of("Settings");
    }
}
```

Open it from whatever triggers your UI, such as a key mapping, a command or a packet. Each loader registers those its own way, so see your loader's documentation for the trigger itself:

```java
Strata.screens().open(new SettingsScreen());
```

`Strata.screens().open` may be called from any thread. Off the render thread, the screen opens on the render thread's next task. The adapter shows it in a new `StrataScreen`, a vanilla `Screen` titled with the screen's `title()`, which vanilla uses for narration. The screen opens in the vanilla theme, and input Strata does not use goes on to vanilla, so Escape still closes it.

Strata's `Screen` and `Component` share their simple names with vanilla's `Screen` and `Component`. Keep Strata screens in classes of their own, or write one of the two with its full name where both are needed.

[Screens](screens.md) covers how Strata screens fit into vanilla's screens, and [Your first screen](../../getting-started/first-screen.md) explains the screen class line by line.

## Loader notes

| | NeoForge | Forge | Fabric | SpongeVanilla |
|---|---|---|---|---|
| Jar | `strata-minecraft-neoforge` | `strata-minecraft-forge` | `strata-minecraft-fabric` | `strata-minecraft-spongevanilla` |
| Contents | `common` and `sponge`, with Strata's `api`, `spi` and `core` jars nested by ModDevGradle's `jarJar` | `common` and `sponge`, with Strata's jars nested by Forge's JarJar Gradle plugin 0.2.3, since ForgeGradle 7.0.40 does not nest jars | `common`, with Strata's jars nested by Loom's `include` | `common` and `sponge`, with the classes of Strata's `api`, `spi` and `core` copied into the jar |
| Entry point | `StrataNeoForge`, `@Mod(value = "strata", dist = Dist.CLIENT)` | `StrataForge`, `@Mod("strata")`, which does nothing on a dedicated server | `StrataFabric`, a `client` entry point | `StrataSpongeVanilla`, `@Plugin("strata")`, on `ConstructPluginEvent` when the game is a client |
| Depends on | NeoForge `[26.3.0.83,)` and Minecraft `[26.3, 26.4)`, with SpongeAPI `[21.0.0-SNAPSHOT,22)` optional and loaded first | Forge `[66.0.13,)` and Minecraft `[26.3, 26.4)`, with SpongeAPI `[21.0.0-SNAPSHOT,22)` optional (`mandatory = false`) and loaded first | Fabric Loader `>=0.19.5`, Minecraft `~26.3` and Java `>=25`; not Fabric API | SpongeAPI `[21.0.0-SNAPSHOT,22)`, loaded first, and Minecraft `[26.3, 26.4)` |
| Side | `CLIENT` on every dependency | `clientSideOnly = true`, and `CLIENT` on every dependency | `"environment": "client"` | Checks the platform type and does nothing on a server |
| Dev client | `./gradlew :neoforge:runClient` | `./gradlew :forge:runClient` | `./gradlew :fabric:runClient` | None |

The NeoForge and Forge entry points call `StrataSponge.install()` when SpongeAPI is loaded and `StrataMinecraft.install()` otherwise. Fabric calls `StrataMinecraft.install()`, and SpongeVanilla `StrataSponge.install()`. Either one hands Strata the adapter's screen controller, so `Strata.screens()` works from the moment Strata's mod is constructed on NeoForge and Forge, from its client entry point on Fabric, and from its `ConstructPluginEvent` on SpongeVanilla. Code that runs before that gets an `IllegalStateException` saying no screen controller is set.

The dev client tasks start a client with Strata loaded from a Strata-Minecraft checkout. Strata on its own opens no screens, so a mod or a small test has to open one. SpongeVanilla has no client task, because its release jars start servers only.

Because `StrataScreen` is an ordinary vanilla screen, loader screen events, such as NeoForge's `ScreenEvent`s, fire for it as for any other screen.

### Sponge

With SpongeNeo on NeoForge or SpongeForge on Forge, Strata stays a mod of that loader and uses Sponge's implementation where Strata has its own; today that is the cause stack. On SpongeVanilla, Strata is a plugin of its own that the client loads.

The code that names Sponge types lives in the `sponge` project, package `org.inspirenxe.strata.sponge`, and is compiled into the SpongeVanilla plugin and the NeoForge and Forge mods. SpongeAPI itself is never nested or copied, since the platform brings it. On NeoForge it is there only when SpongeNeo is installed, and on Forge only with SpongeForge. Both list SpongeAPI as the mod `spongeapi`, so each Strata mod declares it as an optional dependency loaded first, and its entry point asks the mod list before it calls into `org.inspirenxe.strata.sponge`. Nothing else in either jar names a Sponge type, so a game without Sponge never loads one. The NeoForge and Forge tests run without SpongeAPI on the class path to keep it that way.

`StrataSponge.install()` starts the adapter with `SpongeCauseStack`, so Strata's [causes](../../guides/events.md#who-caused-it) are Sponge's. The frames Strata opens for input and the frames you open with `factory.causes().pushFrame()` are Sponge frames, objects you push are on the cause of any Sponge event fired inside them, such as a transaction a button's handler starts, and a Strata event's `cause()` has Sponge's chain and root. Strata's own context keys, `CauseKeys.ORIGIN` among them, stay beside Sponge's context, so a Sponge event does not see them as `EventContextKey`s.

On every loader, input events report `Origin.User` with the local player's `GameProfile` as the user.

### How Strata's core is found

`Strata.texts()`, `appearances()`, `themes()`, `screens()` and the other static methods reach Strata's core through `java.util.ServiceLoader`, with the class loader of Strata's `api` classes. Core lists its provider in `META-INF/services`, and none of the Strata jars has a `module-info`. That works on every loader because Strata's classes end up on one class loader:

- NeoForge (FML 12) reads a nested jar that has no `neoforge.mods.toml` as a library (`NestedLibraryModReader`, type `LIBRARY`). `FMLLoader.loadPlugins` puts the libraries on one class loader, "FML Plugins", where `ServiceLoader` finds core's services file as a resource.
- Forge 66 loads a jar nested in a mod, without `mods.toml` or a mod type in its manifest, as a game library (`JarInJarDependencyLocator`, type `GAMELIBRARY`). `api`, `spi` and `core` become automatic modules of the game layer, which read `META-INF/services` like a class path jar.
- Fabric Loader puts mods and their nested jars on one class loader, Knot, which finds the services file as a resource.
- SpongeVanilla loads each plugin jar as one module and has no nested jars, so the SpongeVanilla jar carries Strata's classes and core's services file itself. This is why a plugin that uses Strata must not copy Strata into its own jar.

`StrataMinecraft.install()` asks `Strata.screens()` for the controller it just handed over and logs an error, without stopping the game, if Strata's core cannot be found or if a different copy of core answers. A broken setup therefore shows in the log at startup.

## More on Minecraft

- [Screens](screens.md): how Strata screens map onto vanilla screens, screens above screens, returning to vanilla screens and pausing.
- [Drawing](drawing.md): units, the canvas, sprites, nine-slices and the vanilla theme.
- [Text and items](text-and-items.md): fonts, formatted text, translations, items and player faces.
- [Container screens](containers.md): menus and slots inside Strata screens.
- [Input](input.md): how vanilla's input reaches Strata, and what differs from Swing.
- [Minecraft help](help.md): common questions, troubleshooting and status.
