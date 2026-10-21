# Strata-Docs

The documentation site for [Strata](../Strata), an engine-agnostic GUI library for Java games. It is a MkDocs Material site whose Java examples are compiled and tested, and whose reference links into Javadoc built from Strata.

| Path | What it is |
|---|---|
| `docs/` | The pages: getting started, guides, platforms, reference, help and project. |
| `src/` | The code the pages include through `--8<--` snippet markers. It compiles against Strata and has its own tests, so every example stays correct. |
| `mkdocs.yml` | The site configuration and navigation. |
| `hooks.py` | Adds the design documents and the roadmap to the Project pages from the Strata checkout. They stay in Strata (`docs/design-implemented.md`, `docs/design-pending.md`, `docs/ROADMAP.md`) because the design-first workflow changes them together with the code. |
| `build.gradle` | Compiles and tests the code in `src`, and `siteJavadoc`, which builds the Javadoc of every Strata library and copies it into `docs/javadoc`. |

Strata-Docs expects the Strata and Strata-Swing repositories next to it (`../Strata` and `../Strata-Swing`). Gradle includes both checkouts through `includeBuild`, the way Strata-Minecraft includes Strata, and the site build reads the design documents from the Strata checkout. Set `STRATA_DIR` to use another location for the documents.

## Building

Building needs JDK 25 and Python 3.

```bash
./gradlew build                       # compile and test the examples against ../Strata
./gradlew siteJavadoc                 # build the Javadoc into docs/javadoc
pip install -r docs/requirements.txt
python -m mkdocs build --strict       # build the site into site/
python -m mkdocs serve                # preview it
```

The example screens in `docs/assets/images` are rendered by `./gradlew renderDocImages`.

When you change Strata's API or controls, update the matching guide, `docs/reference/theme-keys.md` or `docs/reference/defaults.md` here, and the example in `src` the page includes.
