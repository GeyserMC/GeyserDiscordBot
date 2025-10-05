type: text
aliases: mpconnectionfailed, mpconnect, unabletoconnect, unabletoconnecttoworld
title: :x: Multiplayer Connection Failed
issues: Unable to connect to world, Multiplayer connection failed
color: errors

---

This means that the Bedrock client cannot reach the server. This is a network issue and typically caused by improper port forwarding, entering the wrong port in geyser config, or when joining.
Note that if `clone-remote-port=true` in geyser config, then your bedrock port will be ignored and will be the java port instead.
- If you are using a hosting provider, run `/provider` followed by your host in https://discord.com/channels/613163671870242838/613194762249437245.
    - If your host is not in the list, run `!!networkdebug` instead.
- If you are self-hosting, we recommend using playit.gg which can be used to bypass port forwarding.
    - If you are already using playit.gg, the most common issue is confusing the Geyser and playit.gg ports, which are entirely separate. If you changed the bedrock port to match playit.gg, change it back to the default of 19132 and make sure that `clone-remote-port=false`. Join with playit.gg's IP and port.
Additionally, there are various fixes for this on our wiki, which you can get to using the [Wiki page](https://wiki.geysermc.org/geyser/fixing-unable-to-connect-to-world/).
