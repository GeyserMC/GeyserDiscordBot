type: text
issues: please enable it in your BungeeCord config as well

---

This error is caused either by BungeeCord/Waterfall not having IP forwarding enabled, or by a Floodgate mismatched key.pem from the proxy to the backend servers.
This could also be caused by *send-floodgate-data:* being enabled without Floodgate being present on all backend servers.
For more information on how to setup Floodgate API on proxies; Run `!!api` in <#613194762249437245>
