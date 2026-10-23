# Contributing

## Building

You need JDK 25. The Gradle wrapper does the rest.

```bash
./gradlew build
```

This compiles every module, runs every test, and runs Javadoc with every doclint check. It fails on any Javadoc warning, including a missing comment. The documentation lives in its own repository, Strata-Docs, which builds its examples against this one; see [The documentation site](#the-documentation-site).

| Command | Does |
|---|---|
| `./gradlew javadoc` | Builds each module's Javadoc into its `build/docs/javadoc` |
| `./gradlew publishToMavenLocal` | Publishes every module to your local Maven repository |

## Project layout

| Module | Role |
|---|---|
| `api` | What developers compile against: interfaces, values and extension points |
| `spi` | What engine adapters implement and drive, plus the contract test fixtures |
| `core` | The implementation, and in its test fixtures the test kit that applications use to test their screens |
| `build-logic` | The `strata-java` and `strata-documented` convention plugins |

The adapters, the gallery and these docs live in repositories of their own: Strata-Swing (the Java2D/Swing adapter, artifact `strata-swing`), Strata-Vulkan (the Vulkan adapter, artifact `strata-vulkan`), Strata-Minecraft, Strata-Gallery and Strata-Docs. Clone them beside this one to work on several at once. Strata-Swing, Strata-Vulkan, Strata-Minecraft and Strata-Gallery then build against your Strata checkout instead of the published snapshot, and Strata-Docs always builds from the checkouts.

## Rules worth knowing before you change code

- `api`, `spi` and `core` never import an engine type. Everything engine-specific goes through the SPI.
- `api` never imports `spi` or `core`.
- New built-in components are written in `core` as subclasses of `BuiltInComponent`, `BuiltInComposite` or `BuiltInContainer` from `core.node`. They have the same hooks as the public `CustomComponent` classes, plus what only core may use. Anything new a built-in needs from core goes there, never onto the public extension classes.
- Built-ins do not use the public extension classes, so core's tests cover them with components written the way a developer writes them.
- Nothing in `core` reads the wall clock. Use the platform's clock, so tests stay deterministic.
- Painting should not allocate. Cache spans and rectangles during layout.
- Clip and transform pushes are balanced in `try`/`finally`.
- A canceled event skips the component's default `on...` hook. Tests rely on this.

## Code style

Strata follows Wintergreen's style: 4-space indents, K&R braces, `final` on parameters and locals that never change, `this.` on every field and method access, records for value types, and self-typed setters that return `T`. Text is American English (color, behavior, canceled), except identifiers that follow JDK naming, such as `isCancelled`. Every Java source file starts with the Apache License 2.0 header.

## Documentation rules

- Every public and protected type, member and package in the library modules has Javadoc, and the build enforces it.
- API and SPI docs state requirements for implementors: what an implementation must do, what it may assume, threading, null handling, units and coordinate spaces, and what happens on bad input.
- Core and adapter docs state what the class actually does, including defaults and edge cases. Check each claim against the code.
- Interfaces that only Strata implements say so in a plain sentence: "Do not implement this interface."
- Write plainly: no em or en dashes, no bold labels, no dramatic contrasts or closing summaries.

## The documentation site

The site is built with [MkDocs](https://www.mkdocs.org/) and the [Material theme](https://squidfunk.github.io/mkdocs-material/) from the Markdown in `docs/`, and published to GitHub Pages by `.github/workflows/docs.yml` on every push to `main`. It lives in the Strata-Docs repository, which expects the Strata checkout next to it (`../Strata`) and builds against it.

### Building it locally

```bash
pip install -r docs/requirements.txt
./gradlew siteJavadoc
python -m mkdocs serve
```

`mkdocs serve` serves the site at `http://127.0.0.1:8000` and rebuilds on every change. `siteJavadoc` builds each library's Javadoc in Strata and copies it into `docs/javadoc`, which is ignored by Git, so the Javadoc links work. The build is strict: a broken link or a missing snippet fails it.

### Code on the site

Java code on the site is not written in the Markdown. It lives in the `src` folder of Strata-Docs, which `./gradlew build` there compiles and tests against Strata, and pages include it by section:

```markdown
    ```java
    ;--8<-- "layout/LayoutExamples.java:grid"
    ```
```

The path is relative to `src/main/java/org/inspirenxe/strata/examples` (or the matching test directory). The section is marked in the Java file with comments:

```java
// --8<-- [start:grid]
final GridPanel form = factory.gridPanel();
// --8<-- [end:grid]
```

So an API change that breaks an example breaks the build, and the site can never show code that does not compile. Short fragments that only name a method may stay inline in the Markdown; anything a reader might copy belongs in `src`.

### Images

The screenshots in `docs/assets/images` are rendered from the example screens by the Swing adapter. After changing an example, or anything that changes how things draw, render them again:

```bash
./gradlew renderDocImages
```

`RenderDocImages` lists which screen goes to which image.

### Publishing

The workflow builds on every pull request, so a broken page shows up in review, and deploys from `main`. The repository's Pages source must be set to "GitHub Actions" once, under Settings, Pages.
