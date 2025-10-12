type: text
aliases: playit.ggdebug, playitggdebug
title: :tools: playit.gg Debugging
color: help
button: [Setup Guide](https://geysermc.org/wiki/geyser/playit-gg/)

---

The most common issue when setting up playit.gg is confusing the Geyser and playit.gg ports which are entirely separate. To fix this, in Geyser config, change the bedrock port back to the default of 19132 and make sure that `clone-remote-port=false`. If you still have issues, make sure that you:
- Are joining with playit.gg's IP and port.
- Are using the playit.gg program agent and not the playit.gg plugin.
- Have the program agent open while trying to join.
- Have followed the playit.gg setup guide's instructions correctly.