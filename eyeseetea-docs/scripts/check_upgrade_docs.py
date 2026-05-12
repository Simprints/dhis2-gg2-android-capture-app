#!/usr/bin/env python3

from __future__ import annotations

import argparse
import re
import subprocess
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
DOCS_ROOT = ROOT / "eyeseetea-docs"
OPENSPEC_ROOT = ROOT / "openspec"
COMMENT_RE = re.compile(r"EyeSeeTea customization - (.+)")
LINK_RE = re.compile(r"\[[^\]]+\]\(([^)]+)\)")
OPENSPEC_H1_RE = re.compile(r"^#\s+(.+)$")
CODE_EXTENSIONS = {".kt", ".java", ".kts", ".xml"}
FEAT_COMMIT_SHA_RE = re.compile(r"^-\s+`([0-9a-f]{7,40})`\s*[—-]")
INVENTORY_FILE_BULLET_RE = re.compile(r"^-\s+`([^`]+)`")
TOP_LEVEL_SECTION_RE = re.compile(r"^##\s+(\d+)\.")
SUBSECTION_RE = re.compile(r"^#{2,3}\s+2\.\d+\s+(.+)$")


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


def collect_openspec_titles() -> set[str]:
    """Return the set of human titles extracted from openspec/specs/*/spec.md.

    The canonical title is the first `# <Title>` line in each spec.md file.
    This is the string that must match code comments and customization-files.md
    headings after Phase 4 of the onboarding guide.

    Returns an empty set if openspec/ is not present or contains no specs — the
    caller then falls back to the legacy `customization-specs.md` location.
    """
    titles: set[str] = set()
    specs_dir = OPENSPEC_ROOT / "specs"
    if not specs_dir.is_dir():
        return titles
    for spec_file in sorted(specs_dir.rglob("spec.md")):
        try:
            text = spec_file.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            continue
        for line in text.splitlines():
            match = OPENSPEC_H1_RE.match(line.strip())
            if match:
                titles.add(match.group(1).strip())
                break  # only the first H1 is the spec title
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


def parse_inventory_sections(path: Path) -> tuple[dict[str, set[str]], dict[str, list[str]]]:
    """Parse `customization-files.md` into two maps keyed by customization title.

    Returns:
      - inventory_files: title -> set of file paths listed under section 2.N
      - feat_commits: title -> list of commit SHAs listed under section 4.N

    The two sections share the same `### 2.N Title` subheading pattern, so the
    parser switches behavior based on the current top-level `## N.` section.
    """
    inventory_files: dict[str, set[str]] = {}
    feat_commits: dict[str, list[str]] = {}
    if not path.exists():
        return inventory_files, feat_commits

    current_section: str | None = None
    current_title: str | None = None

    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.rstrip()
        top = TOP_LEVEL_SECTION_RE.match(line.strip())
        if top:
            current_section = top.group(1)
            current_title = None
            continue

        sub = SUBSECTION_RE.match(line.strip())
        if sub:
            current_title = sub.group(1).strip()
            if current_section == "2":
                inventory_files.setdefault(current_title, set())
            elif current_section == "4":
                feat_commits.setdefault(current_title, [])
            continue

        if not current_title:
            continue

        stripped = line.strip()
        if current_section == "2":
            match = INVENTORY_FILE_BULLET_RE.match(stripped)
            if match:
                inventory_files[current_title].add(match.group(1).strip())
        elif current_section == "4":
            match = FEAT_COMMIT_SHA_RE.match(stripped)
            if match:
                feat_commits[current_title].append(match.group(1).strip())

    return inventory_files, feat_commits


def commit_touched_files(sha: str) -> tuple[list[str] | None, str | None]:
    """Return the list of files touched by a commit, or (None, error)."""
    result = run_git("show", "--name-only", "--pretty=format:", sha)
    if result.returncode != 0:
        return None, result.stderr.strip() or f"unknown git failure for {sha}"
    return [line for line in result.stdout.splitlines() if line.strip()], None


def file_exists_in_head(rel_path: str) -> bool:
    return (ROOT / rel_path).exists()


def check_feat_commit_coverage(client: str, inventory_path: Path) -> list[str]:
    """For each customization title, verify every code file touched by a feat
    commit is present in the inventory under that title. Silently ignores files
    that no longer exist in HEAD (renamed/moved/deleted upstream) and files
    outside the code-extensions allowlist. Returns one issue line per miss.
    """
    issues: list[str] = []
    inventory_files, feat_commits = parse_inventory_sections(inventory_path)
    if not feat_commits:
        return issues

    titles_without_commits = sorted(set(inventory_files) - set(feat_commits))
    for title in titles_without_commits:
        issues.append(
            f"{client}: inventory section '{title}' has no Feat commits listed in section 4 — "
            "add the original feature commit SHAs so the inventory can be cross-checked"
        )

    for title, shas in sorted(feat_commits.items()):
        listed = inventory_files.get(title, set())
        if not shas:
            issues.append(
                f"{client}: '{title}' has an empty Feat commits list in section 4"
            )
            continue
        for sha in shas:
            touched, err = commit_touched_files(sha)
            if touched is None:
                issues.append(
                    f"{client}: could not read feat commit {sha} for '{title}': {err}"
                )
                continue
            for rel_path in touched:
                if Path(rel_path).suffix not in CODE_EXTENSIONS:
                    continue
                if not file_exists_in_head(rel_path):
                    # File was renamed/moved/deleted upstream — inventory may
                    # track a successor path instead; skip without error.
                    continue
                if rel_path not in listed:
                    issues.append(
                        f"{client}: '{title}' feat commit {sha} touches '{rel_path}' "
                        "but that path is not listed under the corresponding inventory section — "
                        "add it or confirm the file is no longer part of the customization"
                    )
    return issues


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

    # The functional source of truth is `openspec/specs/<capability>/spec.md`
    # (Phase 4 of the onboarding guide). Each spec's first `# <Title>` line is
    # the canonical title used in code comments and in customization-files.md.
    openspec_titles = collect_openspec_titles()
    openspec_available = bool(openspec_titles)

    for client in clients:
        legacy_spec_path = DOCS_ROOT / "customizations" / client / "customization-specs.md"
        inventory_path = DOCS_ROOT / "customizations" / client / "customization-files.md"
        checklist_path = (
            DOCS_ROOT / "upgrade" / client / "upgrade-validation-checklist.md"
        )

        if openspec_available:
            # Post-Phase-4 state: OpenSpec is the functional source of truth.
            spec_titles = set(openspec_titles)
            spec_source_label = "openspec/specs/"
            if legacy_spec_path.exists():
                issues.append(
                    f"{client}: both openspec/specs/ and customization-specs.md exist — "
                    "delete customization-specs.md per onboarding-fork-guide Phase 4"
                )
        else:
            # Brownfield onboarding still in Phase 3: fall back to the narrative draft.
            spec_titles = set(parse_titles(legacy_spec_path, spec_heading_re))
            spec_source_label = f"customizations/{client}/customization-specs.md"
            for issue in parse_status_missing(legacy_spec_path, spec_heading_re):
                issues.append(issue)

        inventory_titles = set(parse_titles(inventory_path, inventory_heading_re))
        checklist_titles = set(parse_titles(checklist_path, spec_heading_re))
        documented_titles.update(spec_titles | inventory_titles)

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
                f"{client}: title mismatch between {spec_source_label} and inventory for '{title}'"
            )

        issues.extend(check_feat_commit_coverage(client, inventory_path))

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
