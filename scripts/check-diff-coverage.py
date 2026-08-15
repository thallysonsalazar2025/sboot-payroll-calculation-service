#!/usr/bin/env python3
"""Require JaCoCo coverage for every executable Java line changed by a PR."""

import argparse
import re
import subprocess
import sys
import xml.etree.ElementTree as ET
from pathlib import Path


def changed_lines(base: str) -> dict[str, set[int]]:
    output = subprocess.check_output(
        ["git", "diff", "--unified=0", f"{base}...HEAD", "--", "src/main/java"], text=True
    )
    result: dict[str, set[int]] = {}
    current = None
    for line in output.splitlines():
        if line.startswith("+++ b/"):
            current = line[6:]
        elif current and line.startswith("@@"):
            added = line.split("+")[1].split(" ")[0]
            start, _, count = added.partition(",")
            length = int(count or "1")
            result.setdefault(current, set()).update(range(int(start), int(start) + length))
    return result


def jacoco_lines(report: Path) -> tuple[dict[tuple[str, int], tuple[int, int]], set[str]]:
    root = ET.parse(report).getroot()
    result = {}
    sources = set()
    for package in root.findall("package"):
        package_name = package.get("name", "").strip("/")
        for source in package.findall("sourcefile"):
            source_name = source.get("name")
            path = f"src/main/java/{package_name}/{source_name}" if package_name else f"src/main/java/{source_name}"
            sources.add(path)
            for line in source.findall("line"):
                result[(path, int(line.get("nr")))] = (
                    int(line.get("mi", "0")), int(line.get("mb", "0"))
                )
    return result, sources


def check(changes: dict[str, set[int]], coverage: dict[tuple[str, int], tuple[int, int]], sources: set[str]) -> list[str]:
    failures = []
    for path, numbers in changes.items():
        if path.endswith(("package-info.java", "module-info.java")):
            continue
        report_path = source_report_path(path)
        if report_path not in sources:
            failures.append(f"{path} (source file missing from JaCoCo report)")
            continue
        for number in sorted(numbers):
            metrics = coverage.get((report_path, number))
            if metrics and (metrics[0] > 0 or metrics[1] > 0):
                failures.append(f"{path}:{number} (missed instructions={metrics[0]}, branches={metrics[1]})")
    return failures


def source_report_path(path: str) -> str:
    source = Path(path)
    if not source.is_file():
        return path
    package = re.search(r"^\s*package\s+([\w.]+)\s*;", source.read_text(encoding="utf-8"), re.MULTILINE)
    if not package:
        return path
    return f"src/main/java/{package.group(1).replace('.', '/')}/{source.name}"


def self_test() -> int:
    uncovered = check(
        {"src/main/java/example/A.java": {7}},
        {("src/main/java/example/A.java", 7): (1, 1)},
        {"src/main/java/example/A.java"},
    )
    missing = check({"src/main/java/example/NewService.java": {5}}, {}, set())
    if not uncovered or not missing:
        print("negative coverage fixture was not rejected", file=sys.stderr)
        return 1
    print("PASS: controlled uncovered-line fixture rejected")
    return 0


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--base")
    parser.add_argument("--report", type=Path)
    parser.add_argument("--self-test", action="store_true")
    args = parser.parse_args()
    if args.self_test:
        return self_test()
    if not args.base or not args.report:
        parser.error("--base and --report are required")
    if not args.report.is_file():
        print(f"JaCoCo report not found: {args.report}", file=sys.stderr)
        return 1
    coverage, sources = jacoco_lines(args.report)
    failures = check(changed_lines(args.base), coverage, sources)
    if failures:
        print("Changed executable lines require 100% line and branch coverage:", file=sys.stderr)
        print("\n".join(failures), file=sys.stderr)
        return 1
    print("PASS: changed executable Java lines have full line and branch coverage")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
