type: text
issues: please enable it in your BungeeCord config as well

---

This error is caused either by bungeecord/waterfall ( Not enabled IP forwarding ), Or by a Floodgate mismatched key.pem from the proxy to the backend servers,
Or you have enabled *send-floodgate-data:* without floodgate being present on all backend servers.
for more information on how to setup floodgate API on proxies; Run `!!api` in <#613194762249437245>
