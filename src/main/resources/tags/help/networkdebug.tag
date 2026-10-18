type: text
aliases: nd

---

If you are unable to connect to your server, this may be because you are using the wrong port, or you do not have a port forwarded as TCP and UDP. Below you can find a list of steps to debug this:
- Check if there is a setup guide for your host on the [setup guide](https://geysermc.org/wiki/geyser/setup).
- Put your server IP address in the `advertise-addresses` that matches your signaling mode (built-in, nxs, or hybrid). Ex: `advertise-addresses: ["ip", "ip2"]`
- Check if the signaling server is available using this link `http://<IP or Domain>:<port>/v1/join` after the server details are filled out. If it doesn't return a response, the Bedrock/WebRTC port isn't forwarded properly.
- Look for a section in your panel for allocating ports, make a new TCP and UDP port allocation and use the port there in the Geyser config. If you can't find a section for this, skip this step.
- If you don't know if your host offers a port allocation forwarded as TCP and UDP, contact your host and ask about getting a port allocation that's TCP and UDP for Geyser.