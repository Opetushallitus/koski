#!/usr/bin/env python3
"""
Build a Slack message of the major updates on Renovate's Dependency Dashboard.

Reads the Detected Dependencies section, so an update stays listed whether it waits
for approval, has an open PR or was ignored. For npm and Maven packages, deps.dev
tells since when a newer major has been available.

Usage: renovate-major-updates.py DASHBOARD_BODY_FILE DASHBOARD_URL > payload.json
"""

import json
import re
import sys
import urllib.parse
import urllib.request


def major(version):
    match = re.match(r"v?(\d+)", version)
    return int(match.group(1)) if match else None


def behind_since(manager, name, current_major):
    if manager not in ("npm", "maven"):
        return None
    url = f"https://api.deps.dev/v3/systems/{manager}/packages/{urllib.parse.quote(name, safe='')}"
    try:
        with urllib.request.urlopen(url, timeout=30) as response:
            versions = json.load(response)["versions"]
    except Exception as error:
        print(f"deps.dev lookup failed for {name}: {error}", file=sys.stderr)
        return None
    stable = [v for v in versions if re.fullmatch(r"[\d.]+", v["versionKey"]["version"]) and v.get("publishedAt")]
    return min((v["publishedAt"][:7] for v in stable if major(v["versionKey"]["version"]) > current_major), default=None)


body_file, dashboard_url = sys.argv[1], sys.argv[2]
body = open(body_file, encoding="utf-8").read()

updates = {}
manager = file = None
for line in body.split("## Detected Dependencies", 1)[-1].splitlines():
    if summary := re.match(r"<details><summary>(.+?)(?: \(\d+\))?</summary>", line):
        if "." in summary.group(1) or "/" in summary.group(1):
            file = summary.group(1)
        else:
            manager = summary.group(1)
    elif dep := re.match(r" - `(\S+) ([^`@]+)\S*` → \[Updates: .*`([^`]+)`\]$", line):
        name, current, target = dep.groups()
        if major(current) is not None and (major(target) or 0) > major(current):
            updates.setdefault((name, current, target), (manager, set()))[1].add(file)

lines = []
for (name, current, target), (manager, files) in updates.items():
    since = behind_since(manager, name, major(current))
    lines.append((since or "9999", f"• `{name}` {current} → {target} · " + (f"behind since {since} · " if since else "") + (files.pop() if len(files) == 1 else f"{len(files)} files")))

message = f":hourglass: *Major updates on the <{dashboard_url}|Dependency Dashboard>: {len(lines)}*\n"
message += "\n".join(line for _, line in sorted(lines))
if abandoned := re.search(r"View abandoned dependencies \((\d+)\)", body):
    message += f"\n\nAbandoned dependencies: {abandoned.group(1)}"
json.dump({"text": message}, sys.stdout, ensure_ascii=False)
