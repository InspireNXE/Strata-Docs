# Installation

## Requirements

| What | Version |
|---|---|
| Java | 25 or newer, for building and at run time |
| Gradle | 9.7.1, through the wrapper in the repository |

Strata itself has no other dependencies. The Swing adapter uses only the JDK. The Vulkan adapter brings LWJGL's Vulkan, VMA, FreeType and stb bindings with their natives. The Minecraft adapter needs the game and a loader; [Minecraft](../platforms/minecraft/index.md#versions-and-loaders) lists the versions.

## Get the artifacts

Strata is published to Maven Central under the group `org.inspirenxe`. Until 0.1.0 is released there are snapshots only, which Central keeps in a repository of its own, so add it next to `mavenCentral()`:

```groovy
repositories {
    mavenCentral()
    maven {
        url = uri('https://central.sonatype.com/repository/maven-snapshots/')
        mavenContent {
            snapshotsOnly()
        }
    }
}
```

| Artifact | Contents | Use it for |
|---|---|---|
| `strata-api` | The API | Compiling screens and components |
| `strata-spi` | The SPI, with the adapter contract tests as test fixtures | Writing an engine adapter |
| `strata-core` | The implementation, with the test kit as test fixtures | Run time only, through an adapter, and `testFixtures` for the [test kit](../guides/testing.md) |
| `strata-swing` | The Swing adapter, built in Strata-Swing | Swing applications and tests |
| `strata-vulkan` | The Vulkan adapter, built in Strata-Vulkan | Applications and games that render with Vulkan through LWJGL |

Every artifact comes with a sources jar and a javadoc jar. A Java 17 build of `strata-api`, `strata-spi` and `strata-core` has `-java17` in its version, such as `0.1.0-java17-SNAPSHOT`.

## Swing applications

Depend on the Swing adapter. It brings `strata-api` and `strata-spi` with it and keeps `strata-core` on the runtime classpath only, so your code compiles against the API alone.

```groovy
dependencies {
    implementation 'org.inspirenxe:strata-swing:0.1.0-SNAPSHOT'
}
```

Then continue with [Your first screen](first-screen.md), or read the [Swing platform page](../platforms/swing.md).

## Vulkan applications

Depend on the Vulkan adapter. Like the Swing adapter, it brings `strata-api` and `strata-spi` and keeps `strata-core` on the runtime classpath only. It also brings LWJGL's Vulkan bindings, which your renderer uses already. To connect it to a GLFW window, add LWJGL's GLFW bindings if your application does not have them.

```groovy
dependencies {
    implementation 'org.inspirenxe:strata-vulkan:0.1.0-SNAPSHOT'
    implementation 'org.lwjgl:lwjgl-glfw:3.4.3'
}
```

Then read the [Vulkan platform page](../platforms/vulkan.md), which shows where `render` goes in a frame.

## Minecraft mods

Depend on the Strata-Minecraft jar for your loader, `strata-minecraft-neoforge`, `-forge` or `-fabric`, or `strata-minecraft-spongevanilla` for a SpongeVanilla plugin, from the same snapshot repository, and add `strata-api` for compiling:

```groovy
dependencies {
    implementation 'org.inspirenxe:strata-minecraft-neoforge:0.1.0-SNAPSHOT'
    compileOnly 'org.inspirenxe:strata-api:0.1.0-SNAPSHOT'
}
```

Players install the Strata jar for their loader next to your mod. [Minecraft setup](../platforms/minecraft/index.md#setup) has the exact dependencies and the metadata entry for each loader.

## Other engines

Strata is a plain Java library. To show screens on another engine, depend on its adapter instead of `strata-swing`. Your screens compile against `strata-api` the same way. [Platforms](../platforms/index.md) lists the adapters, and [Writing an adapter](../platforms/writing-an-adapter.md) shows how to write one for an engine that has none.

## Trying it without a project

Strata-Gallery opens every control in a window, drawn with Java2D or with Vulkan. Clone it and run it; it takes Strata from Maven Central unless Strata is checked out beside it:

```bash
./gradlew runJava2d
./gradlew runVulkan
```

On a machine without a display, render every page to PNGs instead:

```bash
./gradlew run --args="--snapshot gallery"
```

## Building from source

To try a change to Strata in your own project, build it and publish it to your local Maven repository:

```bash
git clone https://github.com/InspireNXE/Strata.git
cd Strata
./gradlew build publishToMavenLocal
```

The build compiles every module, runs the tests and checks that every public type and member is documented. Add `mavenLocal()` to your repositories to pick up the result. Strata-Swing publishes the same way from its own checkout.
