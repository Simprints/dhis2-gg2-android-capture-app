#!/usr/bin/env python3

from __future__ import annotations

import argparse
import re
import subprocess
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
DOCS_ROOT = ROOT / "eyeseetea-docs"
COMMENT_RE = re.compile(r"EyeSeeTea customization - (.+)")
LINK_RE = re.compile(r"\[[^\]]+\]\(([^)]+)\)")


def run_git(*args: str) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        ["git", *args],
        cwd=ROOT,
        text=True,
        capture_output=True,
        check=False,
    )


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Run lightweight consistency checks for EyeSeeTea upgrade docs."
    )
    parser.add_argument("--client", help="Client folder name, for example 'spocc'.")
    parser.add_argument(
        "--base-branch",
        default="develop-eyeseetea",
        help="Base branch used for diff checks. Default: develop-eyeseetea",
    )
    return parser.parse_args()


def collect_markdown_links() -> list[str]:
    errors: list[str] = []
    for path in DOCS_ROOT.rglob("*.md"):
        text = path.read_text(encoding="utf-8")
        for lineno, line in enumerate(text.splitlines(), start=1):
            for raw_link in LINK_RE.findall(line):
                if raw_link.startswith(("http://", "https://", "mailto:", "#")):
                    continue
                target = raw_link.split("#", 1)[0]
                if not target:
                    continue
                resolved = (path.parent / target).resolve()
                if not resolved.exists():
                    rel = path.relative_to(ROOT)
                    errors.append(f"{rel}:{lineno} broken link -> {raw_link}")
    return errors


def parse_titles(path: Path, pattern: re.Pattern[str]) -> list[str]:
    titles: list[str] = []
    if not path.exists():
        return titles
    for line in path.read_text(encoding="utf-8").splitlines():
        match = pattern.match(line.strip())
        if match:
            titles.append(match.group(1).strip())
    return titles


def parse_status_missing(path: Path, heading_re: re.Pattern[str]) -> list[str]:
    if not path.exists():
        return [f"{path.relative_to(ROOT)} missing file"]

    lines = path.read_text(encoding="utf-8").splitlines()
    missing: list[str] = []
    current_title: str | None = None
    has_status = False

    for line in lines + ["### END"]:
        stripped = line.strip()
        match = heading_re.match(stripped)
        if match:
            if current_title and not has_status:
                missing.append(
                    f"{path.relative_to(ROOT)} missing status for '{current_title}'"
                )
            current_title = match.group(1).strip()
            has_status = False
            continue
        if current_title and stripped.startswith("Status:"):
            has_status = True

    return missing


def collect_comment_titles(candidate_files: list[str] | None = None) -> set[str]:
    titles: set[str] = set()
    paths = [ROOT / item for item in candidate_files] if candidate_files else ROOT.rglob("*")
    for path in paths:
        if not path.exists() or not path.is_file():
            continue
        if any(part in {".git", ".gradle", "build"} for part in path.parts):
            continue
        if "eyeseetea-docs" in path.parts:
            continue
        if path.suffix not in {".kt", ".java", ".kts", ".xml"}:
            continue
        try:
            text = path.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            continue
        for line in text.splitlines():
            match = COMMENT_RE.search(line)
            if not match:
                continue
            title = match.group(1).strip()
            title = re.sub(r"\s*-->\s*$", "", title).strip()
            if "[" in title or "]" in title:
                continue
            titles.add(title)
    return titles


def detect_clients() -> list[str]:
    clients: list[str] = []
    base = DOCS_ROOT / "customizations"
    for path in sorted(base.iterdir()):
        if not path.is_dir():
            continue
        if path.name in {"template", "eyeseetea"}:
            continue
        clients.append(path.name)
    return clients


def diff_files(base_branch: str) -> list[str]:
    result = run_git("diff", "--name-only", f"{base_branch}...HEAD")
    if result.returncode != 0:
        return [f"git diff failed against {base_branch}: {result.stderr.strip()}"]
    return [line for line in result.stdout.splitlines() if line.strip()]


def main() -> int:
    args = parse_args()
    clients = [args.client] if args.client else detect_clients()

    issues: list[str] = []
    notes: list[str] = []

    broken_links = collect_markdown_links()
    if broken_links:
        issues.append("Broken markdown links:")
        issues.extend(f"  - {entry}" for entry in broken_links)

    differing = diff_files(args.base_branch)
    if differing:
        notes.append(f"Files still differing against {args.base_branch}:")
        notes.extend(f"  - {entry}" for entry in differing)

    comment_titles = collect_comment_titles(differing if differing else None)

    spec_heading_re = re.compile(r"^#{2,3}\s+\d+\.\s+(.+)$")
    inventory_heading_re = re.compile(r"^#{2,3}\s+2\.\d+\s+(.+)$")
    documented_titles: set[str] = set()

    for client in clients:
        spec_path = DOCS_ROOT / "customizations" / client / "customization-specs.md"
        inventory_path = DOCS_ROOT / "customizations" / client / "customization-files.md"
        checklist_path = (
            DOCS_ROOT / "upgrade" / client / "upgrade-validation-checklist.md"
        )

        spec_titles = set(parse_titles(spec_path, spec_heading_re))
        inventory_titles = set(parse_titles(inventory_path, inventory_heading_re))
        checklist_titles = set(parse_titles(checklist_path, spec_heading_re))
        documented_titles.update(spec_titles | inventory_titles)

        for issue in parse_status_missing(spec_path, spec_heading_re):
            issues.append(issue)
        for issue in parse_status_missing(inventory_path, inventory_heading_re):
            issues.append(issue)

        missing_checklist = sorted((spec_titles | inventory_titles) - checklist_titles)
        for title in missing_checklist:
            issues.append(
                f"{client}: missing checklist entry for customization '{title}'"
            )

        spec_inventory_mismatch = sorted(spec_titles ^ inventory_titles)
        for title in spec_inventory_mismatch:
            issues.append(
                f"{client}: title mismatch between spec and inventory for '{title}'"
            )

    undocumented_comments = sorted(title for title in comment_titles if title not in documented_titles)
    for title in undocumented_comments:
        issues.append(f"code comment title '{title}' not documented in selected client docs")

    if issues:
        output = notes + issues if notes else issues
        print("\n".join(output))
        return 1

    output = notes + ["All upgrade doc checks passed."] if notes else ["All upgrade doc checks passed."]
    print("\n".join(output))
    return 0


if __name__ == "__main__":
    sys.exit(main())
