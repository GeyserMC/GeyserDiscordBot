type: text
aliases: nd

---

If you are unable to connect to your server, this may be because you are using the wrong port, or you do not have a port forwarded as TCP and UDP. Below you can find a list of steps to debug this:
- Check if there is a setup guide for your host on the [setup guide](https://geysermc.org/wiki/geyser/setup).
- Add your server IP to `advertise-addresses` for your signaling mode (built-in, nxs, or hybrid). Example: `advertise-addresses: ["ip", "ip2"]` If you can't find a section for this, skip this step.
- After configuring the server, check `http://<IP or Domain>:<port>/v1/join` (use HTTPS if you have a certificate). No response means the signaling port likely isn't forwarded correctly.
- If your panel supports port allocation, create a TCP/UDP allocation and use its port in the Geyser config. Otherwise, skip this step.
- Unsure if your host supports TCP/UDP forwarding? Contact them to request a TCP/UDP port allocation for Geyser.
