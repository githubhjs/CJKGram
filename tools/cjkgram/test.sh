#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."
out=$(mktemp -d)
trap 'rm -rf "$out"' EXIT
javac -encoding UTF-8 -d "$out" TMessagesProj/src/main/java/org/telegram/messenger/CjkText.java tools/cjkgram/CjkTextTest.java
java -cp "$out" CjkTextTest
