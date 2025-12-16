#!/usr/bin/env bash

# List latest version + publish date for all deps (prod + dev) in key frontends.
# Requires network access and pnpm.

set -euo pipefail

projects=(
  "web"
  "valpas-web"
  "omadata-oauth2-sample/client"
  "omadata-oauth2-sample/server"
  "radiator"
)

for proj in "${projects[@]}"; do
  if [[ ! -d "$proj" ]]; then
    echo "Skipping missing project: $proj" >&2
    continue
  fi
  echo "== $proj =="
  (
    cd "$proj"
    for scope in prod dev; do
      deps_json=$(pnpm ls --${scope} --depth 0 --json 2>/dev/null || true)
      deps=$(SCOPE="$scope" JSON="$deps_json" node - <<'NODE'
const fs = require('fs');
const input = process.env.JSON || '';
const scope = process.env.SCOPE;
try {
  const data = JSON.parse(input || '{}');
  const pkg = Array.isArray(data) ? data[0] : data;
  const target = scope === 'prod' ? pkg.dependencies : pkg.devDependencies;
  if (target) {
    console.log(Object.keys(target).join(' '));
  }
} catch (e) {
  // ignore parse errors
}
NODE
)
      if [[ -z "$deps" ]]; then
        echo "  (no ${scope} dependencies found)"
        continue
      fi
      echo "  -- ${scope} --"
      for dep in $deps; do
        version=$(SCOPE="$scope" DEP="$dep" JSON="$deps_json" node - <<'NODE'
const data = JSON.parse(process.env.JSON || '{}');
const scope = process.env.SCOPE;
const dep = process.env.DEP;
const pkg = Array.isArray(data) ? data[0] : data;
const target = scope === 'prod' ? pkg.dependencies : pkg.devDependencies;
if (!target || !target[dep]) process.exit(1);
const info = target[dep];
if (typeof info === 'string') { console.log(info); process.exit(0); }
if (info.version) { console.log(info.version); }
NODE
) || true

        if [[ -z "$version" ]]; then
          echo "    $dep - failed to get installed version"
          continue
        fi

        out=$(pnpm view "$dep@$version" time --json 2>/dev/null || true)
        if [[ -z "$out" ]]; then
          echo "    $dep@$version - failed to fetch time"
          continue
        fi

        date=$(node -e "const t=$out; const v=t['$version']; if(!v){process.exit(1);} console.log(v);") || date=""
        if [[ -z "$date" ]]; then
          echo "    $dep@$version - missing publish date"
          continue
        fi
        age_days=$(
          node -e "const d=new Date('$date'); const diff= (Date.now()-d.getTime())/86400000; const n=Math.floor(diff); if(Number.isFinite(n)) console.log(n);"
        )
        if [[ -z "$age_days" ]]; then
          echo "    $dep@$version - unable to compute age"
          continue
        fi
        if (( age_days < 14 )); then
          printf "    %-50s %-10s %s (age: %s days)\n" "$dep" "$version" "$date" "$age_days"
        else
          printf "    %-50s %-10s %s\n" "$dep" "$version" "$date"
        fi
      done
    done
  )
  echo
done
