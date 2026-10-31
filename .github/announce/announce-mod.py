#!/usr/bin/env python3
# Posts a mod release to the Announcements channel of the mods Discord server.
# A Discord outage must not fail a release that is already published, so every
# problem here ends in a warning and exit code 0.
import json
import os
import re
import sys
import urllib.request
from datetime import datetime, timezone

COLOURS = {"Alpha": 0xF1C40F, "Beta": 0xE67E22, "Stable": 0x2ECC71}
NOTES_LIMIT = 1500


def clean_notes(text):
    lines = []
    for line in text.splitlines():
        line = line.rstrip()
        if line.startswith("#") or line.startswith("**Full Changelog**"):
            continue
        line = re.sub(r"\s+by @\S+ in \S+$", "", line)
        line = re.sub(r"^\*\s+", "- ", line)
        lines.append(line)
    text = "\n".join(lines).strip()
    if len(text) <= NOTES_LIMIT:
        return text
    cut = text[:NOTES_LIMIT].rsplit("\n", 1)[0]
    return cut + "\n- and more"


def main():
    env = os.environ
    webhook = env.get("WEBHOOK", "")
    if not webhook:
        print("::warning::DISCORD_MODS_WEBHOOK is not set, nothing announced")
        return
    kind = env["KIND"]
    notes = ""
    if env.get("NOTES_FILE") and os.path.exists(env["NOTES_FILE"]):
        with open(env["NOTES_FILE"], encoding="utf-8") as f:
            notes = clean_notes(f.read())
    links = [part.split("|", 1) for part in env["LINKS"].split(";") if part]
    embed = {
        "title": f'{env["MOD_NAME"]} {env["VERSION"]}',
        "description": notes,
        "color": COLOURS[kind],
        "timestamp": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
        "fields": [
            {"name": "Release type", "value": kind, "inline": True},
            {"name": "Minecraft", "value": env["MC_VERSIONS"], "inline": True},
            {"name": "Download", "value": " · ".join(f"[{n}]({u})" for n, u in links), "inline": False},
        ],
        "footer": {"text": f"{kind} release"},
    }
    payload = {"embeds": [embed]}
    if env.get("DRY_RUN") == "true":
        print(json.dumps(payload, indent=2, ensure_ascii=False))
        return
    req = urllib.request.Request(
        webhook,
        data=json.dumps(payload).encode(),
        headers={"Content-Type": "application/json", "User-Agent": "mod-announce/1"},
    )
    with urllib.request.urlopen(req, timeout=20) as resp:
        print(f"Announced {embed['title']} ({kind}): HTTP {resp.status}")


if __name__ == "__main__":
    try:
        main()
    except Exception as exc:  # noqa: BLE001
        print(f"::warning::Announcement failed: {exc}")
        sys.exit(0)
