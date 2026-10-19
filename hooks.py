"""Publishes Strata's engineering documents on the Project pages without copying them.

The design documents and the roadmap stay in the Strata repository, next to the code the design-first workflow changes. The
site build adds them from the Strata checkout beside this one, or from the folder in STRATA_DIR.
"""
import os
from pathlib import Path

from mkdocs.structure.files import File

DOCUMENTS = ('design-implemented.md', 'design-pending.md', 'ROADMAP.md')


def on_files(files, config):
    root = Path(config.config_file_path).parent
    strata = Path(os.environ.get('STRATA_DIR', root.parent / 'Strata')).resolve()
    for name in DOCUMENTS:
        source = strata / 'docs' / name
        if not source.is_file():
            raise FileNotFoundError(f'{source} not found. Check out Strata next to Strata-Docs, or set STRATA_DIR.')
        files.append(File.generated(config, name, abs_src_path=str(source)))
    return files
