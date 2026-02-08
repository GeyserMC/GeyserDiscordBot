type: text
aliases: nd

---

If you are unable to connect to your server, this may be because you are using the wrong port, or you do not have a UDP port forwarded. Below you can find a list of steps to debug this:
- Check if there is a setup guide for your host on the [setup guide](https://geysermc.org/wiki/geyser/setup).
- Enable `clone-remote-port` in the Geyser config, restart, and try join with your Java IP and port.
- Look for a section in your panel for allocating ports, make a new allocation and use the port there in the Geyser config (ensuring you disable `clone-remote-port`). If you can't find a section for this, skip this step.
- Contact your host and ask about getting a UDP port allocation. If you don't know how to explain that, ask for a port to run Geyser.
