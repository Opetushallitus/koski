#!/usr/bin/env bash
# list-ci-deploys - list Koski deployments via the GitHub Deployments API.
#
# This is the GitHub-side view: it reflects what the deploy workflow
# successfully completed for each environment, NOT the live ECS state.
# Pending-approval ("waiting") and failed deploys are filtered out.
# For live ECS state, query ECS directly.
#
# Usage:
#   list-ci-deploys [<env>]                       # snapshot at "now"
#   list-ci-deploys [<env>] --at <ts>             # snapshot at <ts>
#   list-ci-deploys [<env>] --from <ts> [--until <ts>]   # range, --until defaults to "now"
#
#   env:           dev | qa | prod | all (default: all)
#   ts:            ISO 8601 — naive ts is Europe/Helsinki; "...Z"/"...+03:00" is
#                  honoured; the literal "now" resolves to the current time.
#
# Snapshot prints the most-recently-successful deployment per env at the given
# instant. Range prints all successful deployments in the window, sorted
# ascending. Output is an aligned table.

set -euo pipefail

repo=${REPO:-Opetushallitus/koski}

env=all
at=""
from=""
until_=""

print_help() { sed -n '2,20p' "$0" | sed 's/^# \{0,1\}//'; }

while [[ $# -gt 0 ]]; do
  case $1 in
    --at)    at=${2:?--at requires a value};    shift 2;;
    --from)  from=${2:?--from requires a value}; shift 2;;
    --until) until_=${2:?--until requires a value}; shift 2;;
    -h|--help) print_help; exit 0;;
    -*) >&2 echo "Unknown flag: $1"; exit 2;;
    *)  env=$1; shift;;
  esac
done

if [[ -n $at && ( -n $from || -n $until_ ) ]]; then
  >&2 echo "Error: --at cannot be combined with --from/--until"; exit 2
fi
if [[ -n $until_ && -z $from ]]; then
  >&2 echo "Error: --until requires --from"; exit 2
fi

case $env in
  dev|qa|prod) envs=("$env");;
  all)         envs=(dev qa prod);;
  *) >&2 echo "Error: env must be one of dev|qa|prod|all (got: $env)"; exit 2;;
esac

parse_ts() {
  python3 - "$1" <<'PY'
import sys
from datetime import datetime, timezone
from zoneinfo import ZoneInfo
s = sys.argv[1]
if s == "now":
    print(datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"))
else:
    s = s.replace("Z", "+00:00")
    t = datetime.fromisoformat(s)
    if t.tzinfo is None:
        t = t.replace(tzinfo=ZoneInfo("Europe/Helsinki"))
    print(t.astimezone(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"))
PY
}

# Pull the most recent deployment to <env> with created_at <= <cutoff> whose
# status history includes "success". Emits one JSON object on stdout, or
# nothing if no match.
query_snapshot() {
  local e=$1 cutoff=$2 candidates=() line dep id succeeded
  while IFS= read -r line; do
    candidates+=("$line")
  done < <(
    gh api --paginate "/repos/${repo}/deployments?environment=${e}&per_page=100" \
      --jq ".[] | select(.created_at <= \"${cutoff}\") | @json" \
    | head -50
  )
  for dep in "${candidates[@]}"; do
    id=$(jq -r .id <<<"$dep")
    succeeded=$(gh api "/repos/${repo}/deployments/${id}/statuses" \
                  --jq 'map(.state) | contains(["success"])')
    if [[ $succeeded == true ]]; then
      jq --arg env "$e" '{
        env: $env, sha, short_sha: .sha[0:12], ref, created_at, creator: .creator.login
      }' <<<"$dep"
      return 0
    fi
  done
  return 1
}

# Stream successful deployments to <env> within [from, until] as JSON objects
# (one per line). Filters out waiting/failure/inactive-only deployments.
query_range() {
  local e=$1 from=$2 until=$3 dep id succeeded
  while IFS= read -r dep; do
    [[ -z $dep ]] && continue
    id=$(jq -r .id <<<"$dep")
    succeeded=$(gh api "/repos/${repo}/deployments/${id}/statuses" \
                  --jq 'map(.state) | contains(["success"])')
    if [[ $succeeded == true ]]; then
      printf '%s\n' "$dep"
    fi
  done < <(
    gh api --paginate "/repos/${repo}/deployments?environment=${e}&per_page=100" \
      --jq ".[] | select(.created_at >= \"${from}\" and .created_at <= \"${until}\")
            | {env: \"${e}\", sha, short_sha: .sha[0:12], ref, created_at, creator: .creator.login, id}
            | @json"
  )
}

# Read JSON objects from stdin, emit a sorted aligned table.
format_table() {
  {
    printf 'ENV\tDEPLOYED (Helsinki)\tSHA\tREF\tCREATOR\n'
    TZ=Europe/Helsinki jq -rs '
      sort_by(.created_at) | .[] |
      [ .env,
        (.created_at | fromdate | strflocaltime("%Y-%m-%d %H:%M")),
        .short_sha,
        .ref,
        .creator
      ] | @tsv'
  } | column -t -s $'\t'
}

if [[ -n $from ]]; then
  from_ts=$(parse_ts "$from")
  until_ts=$(parse_ts "${until_:-now}")
  >&2 echo "Successful deployments to ${envs[*]} between ${from_ts} and ${until_ts} (UTC)..."
  {
    for e in "${envs[@]}"; do
      query_range "$e" "$from_ts" "$until_ts"
    done
  } | format_table
else
  cutoff=$(parse_ts "${at:-now}")
  >&2 echo "Deployments to ${envs[*]} at or before ${cutoff} (UTC)..."
  {
    for e in "${envs[@]}"; do
      query_snapshot "$e" "$cutoff" || >&2 echo "  no successful deployment for env=${e}"
    done
  } | format_table
fi
