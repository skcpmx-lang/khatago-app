#!/usr/bin/env sh

if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi

echo "Gradle is not installed. Install Gradle or run the GitHub Actions workflow." >&2
exit 1
