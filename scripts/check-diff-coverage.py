#!/usr/bin/env python3
"""Require JaCoCo coverage for every executable Java line changed by a PR."""

import argparse
import subprocess
import sys
import xml.etree.ElementTree as ET
from pathlib import Path


def changed_lines(base: str) -> dict[str, set[int]]:
    output = subprocess.check_output(
        ["git", "diff", "--unified=0", f"{base}...HEAD", "--", "*.java"], text=True
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


def jacoco_lines(report: Path) -> dict[tuple[str, int], tuple[int, int]]:
    root = ET.parse(report).getroot()
    result = {}
    for package in root.findall("package"):
        package_name = package.get("name", "")
        for source in package.findall("sourcefile"):
            path = f"src/main/java/{package_name}/{source.get('name')}"
            for line in source.findall("line"):
                result[(path, int(line.get("nr")))] = (
                    int(line.get("mi", "0")), int(line.get("mb", "0"))
                )
    return result


def check(changes: dict[str, set[int]], coverage: dict[tuple[str, int], tuple[int, int]]) -> list[str]:
    failures = []
    for path, numbers in changes.items():
        for number in sorted(numbers):
            metrics = coverage.get((path, number))
            if metrics and (metrics[0] > 0 or metrics[1] > 0):
                failures.append(f"{path}:{number} (missed instructions={metrics[0]}, branches={metrics[1]})")
    return failures


def self_test() -> int:
    failures = check({"src/main/java/example/A.java": {7}}, {("src/main/java/example/A.java", 7): (1, 1)})
    if not failures:
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
    failures = check(changed_lines(args.base), jacoco_lines(args.report))
    if failures:
        print("Changed executable lines require 100% line and branch coverage:", file=sys.stderr)
        print("\n".join(failures), file=sys.stderr)
        return 1
    print("PASS: changed executable Java lines have full line and branch coverage")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
