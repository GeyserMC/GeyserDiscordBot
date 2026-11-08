# GeyserBot
[![forthebadge made-with-java](https://forthebadge.com/badges/made-with-java.svg)](https://java.com/)

[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Build Status](https://github.com/GeyserMC/GeyserDiscordBot/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/GeyserMC/GeyserDiscordBot/actions/workflows/build.yml)
[![Discord](https://img.shields.io/discord/613163671870242838.svg?color=%237289da&label=discord)](http://discord.geysermc.org/)

A bot for the GeyserMC Discord server

# How to run
- Download the latest build from [GitHub Actions](https://github.com/GeyserMC/GeyserDiscordBot/actions/workflows/build.yml) or build it yourself with `./gradlew build`
- Configure a `bot.properties` file based on the example
- Start the bot with `java -jar GeyserBot.jar` or put the systemd unit file into `/etc/systemd/system/` and `sudo systemctl daemon-reload && sudo systemctl enable --now geyserbot`

# Server preferences
These allow customisation of the bot on a per-guild basis. Use `/settings` with the `get`, `set`, `add`, or `remove` action and the preference key. `set` replaces the preference value; `add` and `remove` are intended for comma-separated lists.

Most preferences are comma-separated lists. Map-style preferences use comma-separated `key|value` pairs.

| Preference | Description | Example value |
|---|---|---|
| `allowed-invites` | Guild IDs for allowed invites | `613163671870242838,393465748535640064` |
| `banned-domains` | Domains whose links are removed from messages | `dlscord-nitro.click,discord-airdrop.com` |
| `banned-ips` | IPs that cause a message to be removed when a linked domain resolves to them | `95.181.163.44,95.181.163.46` |
| `check-domains` | Domains checked to filter phishing and malware links | `steamcommunity.com` |
| `convert-extensions` | File extensions automatically uploaded to paste.gg | `txt,log,yml,log.0` |
| `dont-level` | Channel IDs excluded from leveling; set to `0` to disable leveling server-wide | `754458074818805811` |
| `dont-log` | Channel IDs excluded from moderation logs | `613168850925649981,613168974645035016` |
| `donation-feeds-channel` | Channel ID where Open Collective webhook messages are posted | `873968290836385792` |
| `exploit-reports-notify-channel` | Channel ID notified when an exploit report is made | `614877230811709474` |
| `forum-auto-reply-tag` | Embed tag sent in new support forum posts; leave unset to disable | `help` |
| `forum-channel` | Forum channel ID used for support forum management | `645877230811709456` |
| `health-checks` | Channel IDs and URLs for periodic web health checks | `808759445827223572\|https://api.geysermc.org/health` |
| `honey-pot-channel` | Channel ID monitored as a scam-account honeypot | `614877230811709474` |
| `log-channel` | Channel ID for moderation logs | `614877230811709474` |
| `moderation-channel` | Channel ID used for moderation actions | `614877230811709474` |
| `moderation-role` | Role ID pinged for moderation actions | `856161072507518986` |
| `post-close-roles` | Roles (names or IDs) allowed to close any forum post; if unset, no extra role restriction is applied | `moderator,856161072507518986` |
| `preview-channel` | Forum channel ID where preview posts are created | `614877230811709474` |
| `preview-feeds` | Forum channel ID for preview feeds | `614877230811709474` |
| `preview-feeds-channel` | News channel ID monitored for pull request preview links | `614877230811709474` |
| `punishment-message` | Message appended to punishment DMs | `If you wish to appeal your punishment, contact an admin.` |
| `roles` | Role names and IDs available through the `role`/`rank` command | `GeyserNews\|613169070367309834,Tester\|775083359101517884` |
| `rss-feeds` | Channel IDs and RSS feed URLs to announce new posts | `916482484122824704\|https://blog.geysermc.org/feed.xml` |
| `update-channel` | Channel ID for Minecraft update notifications | `618189671259832320` |
| `voice-role` | Role granted to users when they join a voice channel | `856161072507518986` |

## Forum and preview tools
Configure `forum-channel` to enable the `/post` tools for creating, renaming, tagging, and closing support posts, including bulk-closing old posts. `post-close-roles` optionally limits who can close posts, and `forum-auto-reply-tag` selects the embed tag automatically sent in new support posts.

To publish pull request previews, set `preview-feeds-channel` to the news channel where PR links are posted and `preview-channel` to the destination forum. GeyserMC pull request links in the news channel are turned into forum posts with discussion and build-download links.

## Exploit reports and honeypot
Use `/reportexploit` to post the report button in a channel. Reports are created as private threads there; set `exploit-reports-notify-channel` if staff should also receive a notification in a separate channel. Messages sent to `honey-pot-channel` trigger the bot's scam-account quarantine handling.
