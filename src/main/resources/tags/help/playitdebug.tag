type: text
aliases: playit.ggdebug, playitggdebug
title: :geyser: playit.gg Debugging
color: help
button: [playit.gg Setup Guide](https://geysermc.org/wiki/geyser/playit-gg/)

---

The most common issue when setting up playit.gg is confusing the bedrock port and the playit.gg port which are entirely separate. To fix this, change the bedrock port back to the default of `19132` and ensure that `clone-remote-port` is set to `false` in the Geyser config.

If you still have issues, make sure that:
- You are joining with playit.gg's IP and port.
- You are using the playit.gg program agent and not the playit.gg plugin.
- You have the program agent open while trying to join.
- You have followed the playit.gg setup guide's instructions correctly.