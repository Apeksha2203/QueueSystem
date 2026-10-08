"""Build the student frontend preparation PDF with the shared guide style."""
import importlib.util
from pathlib import Path

root = Path(__file__).resolve().parents[1]
spec = importlib.util.spec_from_file_location('preparation_guides', root / 'scripts/build-preparation-pdfs.py')
guides = importlib.util.module_from_spec(spec)
spec.loader.exec_module(guides)
guides.build(
    'Campus-Queue-Student-Frontend-Preparation.pdf',
    'Student frontend presentation preparation',
    'Your speaking notes, packages, files and viva answers',
    root / 'docs/student-frontend-preparation.md',
)
