#!/usr/bin/env bash
set -euo pipefail

# Quick tester for the Renarde demo app:
# - Verifies /renarde renders
# - Fetches CSRF from /Todos/todos
# - Posts a new todo
# - Confirms it appears on the Todos page

BASE_URL="${BASE_URL:-http://localhost:8080}"
TASK="${1:-Added from script}"

TMP_DIR="$(mktemp -d)"
cleanup() { rm -rf "$TMP_DIR" || true; }
trap cleanup EXIT

COOKIE_JAR="$TMP_DIR/cookies.txt"
INDEX_HTML="$TMP_DIR/index.html"
TODOS_HTML="$TMP_DIR/todos.html"
RESP_HEADERS="$TMP_DIR/headers.txt"

echo "[1/4] Checking $BASE_URL/renarde ..."
HTTP_CODE=$(curl -sS -o "$INDEX_HTML" -w '%{http_code}' "$BASE_URL/renarde")
if [[ "$HTTP_CODE" != "200" ]]; then
  echo "ERROR: /renarde returned HTTP $HTTP_CODE" >&2
  exit 1
fi
if ! grep -q 'Welcome to your Quarkus Renarde' "$INDEX_HTML"; then
  echo "ERROR: Unexpected /renarde content" >&2
  exit 1
fi
echo "OK: /renarde looks good"

echo "[2/4] Getting CSRF token from $BASE_URL/Todos/todos ..."
curl -sS -c "$COOKIE_JAR" "$BASE_URL/Todos/todos" -o "$TODOS_HTML"
# Extract CSRF token value from hidden input
CSRF_VALUE="$(sed -n 's/.*name="csrf-token" value="\([^"]*\)".*/\1/p' "$TODOS_HTML" | head -n1)"
if [[ -z "${CSRF_VALUE:-}" ]]; then
  echo "ERROR: Could not extract CSRF token from Todos page" >&2
  exit 1
fi
echo "OK: CSRF token acquired"

echo "[3/4] Posting new todo: $TASK ..."
curl -sS -b "$COOKIE_JAR" -X POST -H 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode "csrf-token=$CSRF_VALUE" \
  --data-urlencode "task=$TASK" \
  -D "$RESP_HEADERS" -o /dev/null \
  "$BASE_URL/Todos/add"
if ! grep -q "303" "$RESP_HEADERS"; then
  echo "WARN: Expected 303 redirect after POST (continuing)" >&2
fi
echo "OK: POST submitted"

echo "[4/4] Verifying the todo appears on the page ..."
curl -sS -b "$COOKIE_JAR" "$BASE_URL/Todos/todos" -o "$TODOS_HTML"
# The template capitalises the first letter of each word; emulate for matching
CAP_TASK="$(printf '%s' "$TASK" | awk '{for(i=1;i<=NF;i++){ $i=toupper(substr($i,1,1)) substr($i,2)}; print}')"
if grep -iq "^[[:space:]]*$CAP_TASK[[:space:]]*$" "$TODOS_HTML"; then
  echo "SUCCESS: Found todo on page -> $CAP_TASK"
else
  echo "ERROR: Could not find todo '$CAP_TASK' on Todos page" >&2
  exit 1
fi

echo "Done."


