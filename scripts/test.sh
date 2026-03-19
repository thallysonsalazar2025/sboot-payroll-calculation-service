#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
BUILD_DIR="$ROOT_DIR/build"
MAIN_OUT="$BUILD_DIR/main"
TEST_OUT="$BUILD_DIR/test"
rm -rf "$BUILD_DIR"
mkdir -p "$MAIN_OUT" "$TEST_OUT"
find "$ROOT_DIR/src/main/java" -name '*.java' -print > "$BUILD_DIR/main-sources.txt"
find "$ROOT_DIR/src/test/java" -name '*.java' -print > "$BUILD_DIR/test-sources.txt"
javac --release 21 -d "$MAIN_OUT" @"$BUILD_DIR/main-sources.txt"
javac --release 21 -cp "$MAIN_OUT" -d "$TEST_OUT" @"$BUILD_DIR/test-sources.txt"
java -cp "$MAIN_OUT:$TEST_OUT" br.com.payroll.calculation.support.TestRunner
