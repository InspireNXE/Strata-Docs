# Installation

## Requirements

| What | Version |
|---|---|
| Java | 25 or newer, for building and at run time |
| Gradle | 9.7.1, through the wrapper in the repository |

Strata itself has no other dependencies. The Swing adapter uses only the JDK.

## Get the artifacts

Strata is not published to a public Maven repository yet. Until it is, build it from source.

```bash
git clone https://github.com/InspireNXE/Strata.git
cd Strata
./gradlew build
```

The build compiles every module, runs the tests and checks that every public type and member is documented. The Swing adapter has a repository of its own, Strata-Swing, which builds against a Strata checkout beside it, so clone it next to Strata and run `./gradlew build` there too. The jars land in each module's `build/libs`:

| Jar | Contents | Use it for |
|---|---|---|
| `strata-api` | The API | Compiling screens and components |
| `strata-spi` | The SPI | Writing an engine adapter |
| `strata-core` | The implementation | Run time only, through an adapter |
| `strata-canvas-swing` | The Swing adapter, built in Strata-Swing | Swing applications and tests |

To use these from another Gradle project, publish them to your local Maven repository, in Strata and then in Strata-Swing:

```bash
./gradlew publishToMavenLocal
```

They are then available as `org.inspirenxe:strata-<module>:0.1.0-SNAPSHOT` from `mavenLocal()`.

!!! warning "Not yet tried from outside the repository"
    The published metadata has not been consumed from a separate project yet. The snippet below follows the build's own setup. If it fails for you, please open an issue with the error.

## Swing applications

Depend on the Swing adapter. It brings `strata-api` and `strata-spi` with it and keeps `strata-core` on the runtime classpath only, so your code compiles against the API alone.

```groovy
repositories {
    mavenLocal()
}

dependencies {
    implementation 'org.inspirenxe:strata-canvas-swing:0.1.0-SNAPSHOT'
}
```

Then continue with [Your first screen](first-screen.md), or read the [Swing platform page](../platforms/swing.md).

## Other engines

Strata is a plain Java library. To show screens on an engine other than Swing, depend on that engine's adapter instead of `strata-canvas-swing`. Your screens compile against `strata-api` the same way. [Platforms](../platforms/index.md) lists the adapters, and [Writing an adapter](../platforms/writing-an-adapter.md) shows how to write one for an engine that has none.

## Trying it without a project

The Strata-Gallery repository, checked out beside Strata, opens every control in a window:

```bash
./gradlew run
```

On a machine without a display, render every page to PNGs instead:

```bash
./gradlew run --args="--snapshot gallery"
```
