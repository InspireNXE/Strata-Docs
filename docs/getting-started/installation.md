# Installation

## Requirements

| What | Version |
|---|---|
| Java | 25 or newer, for building and at run time |
| Gradle | 9.7.1, through the wrapper in the repository |

Strata itself has no other dependencies. The Swing adapter uses only the JDK.

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
| `strata-core` | The implementation | Run time only, through an adapter |
| `strata-swing` | The Swing adapter, built in Strata-Swing | Swing applications and tests |

Every artifact comes with a sources jar and a javadoc jar. A Java 17 build of `strata-api`, `strata-spi` and `strata-core` has `-java17` in its version, such as `0.1.0-java17-SNAPSHOT`.

## Swing applications

Depend on the Swing adapter. It brings `strata-api` and `strata-spi` with it and keeps `strata-core` on the runtime classpath only, so your code compiles against the API alone.

```groovy
dependencies {
    implementation 'org.inspirenxe:strata-swing:0.1.0-SNAPSHOT'
}
```

Then continue with [Your first screen](first-screen.md), or read the [Swing platform page](../platforms/swing.md).

## Other engines

Strata is a plain Java library. To show screens on an engine other than Swing, depend on that engine's adapter instead of `strata-swing`. Your screens compile against `strata-api` the same way. [Platforms](../platforms/index.md) lists the adapters, and [Writing an adapter](../platforms/writing-an-adapter.md) shows how to write one for an engine that has none.

## Trying it without a project

Strata-Gallery opens every control in a window. Clone it and run it; it takes Strata from Maven Central unless Strata is checked out beside it:

```bash
./gradlew run
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
