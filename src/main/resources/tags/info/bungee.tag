type: text
aliases: bungeecord, velocity

---

The Geyser and Floodgate BungeeCord plugins only need to be installed on the BungeeCord proxy unless you intend to use the Floodgate API. That way, you need Floodgate on the "backend" servers too.
Installing Floodgate on the backend servers also makes it so that Bedrock players don't have to switch backend servers for their skins to start displaying.

To properly set up Floodgate on the proxy <-> Floodgate on the backend connection, do the following:
1. Set up player-info-forwarding (Velocity) or enable ip-forward (BungeeCord & forks) in the proxy configuration.
2. Replace the Floodgate key.pem file on the backend servers (the servers behind the proxy) with the key.pem file from the proxy.
3. Enable "send-floodgate-data" in the Floodgate config.yml on the Proxy. Be careful - this requires ALL the backend servers to have floodgate installed.

Run `!!api` in <#613194762249437245> for more information regarding the Floodgate API.
