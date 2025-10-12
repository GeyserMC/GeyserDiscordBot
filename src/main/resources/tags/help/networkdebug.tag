type: text
title: :tools: Network Debugging
color: help
button: [Setup Guide](https://geysermc.org/wiki/geyser/setup)

---

If you are unable to connect to your server, it may be because you are using the wrong port or have not forwarded a UDP port. Below is a list of solutions to help you solve this issue:
- Check if there is a setup guide for your host on the Geyser setup guide.
- Enable `clone-remote-port` in the Geyser config, restart, and try join with your Java IP and port.
- In your panel, look for a section for allocating ports, create a new allocation, and use that port in the Geyser config.
└> If you can't find a section for this, skip this step.
└> Please ensure you have disabled `clone-remote-port` in the Geyser config before doing this.
- Contact your host and ask about getting a UDP port allocation.
└> If you don't know how to explain that, ask for a port to run Geyser.