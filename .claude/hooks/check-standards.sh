#!/usr/bin/env bash
# Claude Code Stop hook: before Claude finishes a turn, run ./gradlew check if any code or
# build files have uncommitted changes. If it fails, Claude is blocked from stopping and
# shown the errors, so it has to fix them first.
#
# To avoid an endless loop, a second consecutive failure lets Claude stop but warns you.
set -uo pipefail
cd "${CLAUDE_PROJECT_DIR:-$(git rev-parse --show-toplevel)}"

input=$(cat)
already_blocked=$(printf '%s' "$input" | jq -r '.stop_hook_active // false' 2>/dev/null || echo false)

changed=$(git status --porcelain -- '*.java' '*.kts' '*.xml' '*.toml' '*.properties' '.githooks/*' 2>/dev/null)
[ -z "$changed" ] && exit 0

if output=$(./gradlew --quiet check 2>&1); then
    exit 0
fi

summary=$(printf '%s\n' "$output" | grep -vE '^Picked up ' | tail -40)
if [ "$already_blocked" = "true" ]; then
    jq -n --arg s "Coding-standards check still fails after a retry. Run ./gradlew check to see why." \
        '{systemMessage: $s}'
    exit 0
fi

{
    echo "./gradlew check failed. Fix these problems before finishing"
    echo "(run ./gradlew spotlessApply for formatting issues):"
    echo
    printf '%s\n' "$summary"
} >&2
exit 2
