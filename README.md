<p align="center"><img src=".github/assets/logo.svg" width="128" alt="Strata-Docs logo"></p>

# Strata-Docs

The source of the [documentation site](https://inspirenxe.github.io/Strata/) for [Strata](https://github.com/InspireNXE/Strata). It is a MkDocs Material site whose Java examples are compiled and tested, and whose reference links into Javadoc built from Strata.

| Path | What it is |
|---|---|
| `docs/` | The pages: getting started, guides, platforms, reference, help and project. |
| `src/` | The code the pages include through `--8<--` snippet markers, compiled and tested against Strata. |
| `hooks.py` | Pulls the design documents and roadmap in from the Strata checkout. Set `STRATA_DIR` to point elsewhere. |

## Building

Building needs JDK 25, Python 3, and the Strata and Strata-Swing checkouts beside this one, at `../Strata` and `../Strata-Swing`.

```bash
./gradlew build
./gradlew siteJavadoc
pip install -r docs/requirements.txt
python -m mkdocs serve
```

`./gradlew build` compiles and tests the examples, and `siteJavadoc` builds the Javadoc into `docs/javadoc`. Use `python -m mkdocs build --strict` for a full build into `site/`. `./gradlew renderDocImages` renders the example screens in `docs/assets/images`.

When Strata's API or controls change, update the matching guide or reference page here and the example in `src` it includes.

## License

Strata-Docs is released under the Apache License, Version 2.0. See [LICENSE](LICENSE) and [NOTICE](NOTICE).
