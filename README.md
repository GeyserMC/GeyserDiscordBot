# GeyserBot
[![forthebadge made-with-java](https://forthebadge.com/images/badges/made-with-java.svg)](https://java.com/)

[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Build Status](https://ci.opencollab.dev/job/GeyserMC/job/GeyserDiscordBot/job/master/badge/icon)](https://ci.opencollab.dev/job/GeyserMC/job/GeyserDiscordBot/job/master/)
[![Discord](https://img.shields.io/discord/613163671870242838.svg?color=%237289da&label=discord)](http://discord.geysermc.org/)

A bot for the GeyserMC Discord server

# How to run
- Download the latest build from the CI ([here](https://ci.opencollab.dev/job/GeyserMC/job/GeyserDiscordBot/job/master/lastSuccessfulBuild/artifact/target/GeyserBot.jar))
- Configure a `bot.properties` file based on the example
- Start the bot with `java -jar GeyserBot.jar` or put the systemd unit file into `/etc/systemd/system/` and `sudo systemctl daemon-reload && sudo systemctl enable --now geyserbot`

# Server preferences
These allow customisation of the bot on a per guild basis. These can be changed using `!settings set preference value` and checked using `!settings get preference`.

Most of these are lists which values are seperated by `,` and a few are seperated by a `|` aswell which gives them a more key-value like structure.
| Preference           | Description                                                          | Example value                                               |
|----------------------|----------------------------------------------------------------------|-------------------------------------------------------------|
| `convert-extensions` | File extensions to auto upload to paste.gg                           | `txt,log,yml,log.0`                                         |
| `log-channel`        | Channel ID to output moderation logs to                              | `614877230811709474`                                        |
| `update-channel`     | Channel ID to notify with minecraft updates                          | `618189671259832320`                                        |
| `roles`              | Roles that are available to join with the `role`/`rank` command      | `GeyserNews\|613169070367309834,Tester\|775083359101517884` |
| `voice-role`         | The role to grant users when in a voice channel                      | `856161072507518986`                                        |
| `dont-log`           | Channels IDs that are exlcuded from the moderation log               | `613168850925649981,613168974645035016`                     |
| `check-domains`      | Domains to check against to filter phishing and malware domains      | `steamcommunity.com`                                        |
| `health-checks`      | Channel IDs to log web health checks to along with the url to check  | `808759445827223572\|https://api.geysermc.org/health`       |
