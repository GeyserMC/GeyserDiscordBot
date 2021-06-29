# GeyserBot
[![forthebadge made-with-java](https://ForTheBadge.com/images/badges/made-with-java.svg)](https://java.com/)

[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Build Status](https://ci.opencollab.dev/job/GeyserMC/job/GeyserDiscordBot/job/master/badge/icon)](https://ci.opencollab.dev/job/GeyserMC/job/GeyserDiscordBot/job/master/)
[![Discord](https://img.shields.io/discord/613163671870242838.svg?color=%237289da&label=discord)](http://discord.geysermc.org/)

A bot for the GeyserMC Discord server

# How to run
- Download the latest build from the CI ([here](https://ci.opencollab.dev/job/GeyserMC/job/GeyserDiscordBot/job/master/lastSuccessfulBuild/artifact/target/GeyserBot.jar))
- Configure a `bot.properties` file based on the example
- Start the bot with `java -jar GeyserBot.jar` or put the systemd unit file into `/etc/systemd/system/` and `sudo systemctl daemon-reload && sudo systemctl enable --now geyserbot`
