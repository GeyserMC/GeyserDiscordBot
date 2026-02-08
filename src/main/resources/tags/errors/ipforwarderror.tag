type: text
title: :x: IP Forwarding Error
aliases: ipforward
issues: please enable it in your BungeeCord config as well
color: errors

---

This error can be caused by the following:
- `ip_forward` is not enabled in your BungeeCord proxy `config.yml`.
- [Player information forwarding](https://docs.papermc.io/velocity/player-information-forwarding/) is not set up on your Velocity proxy.
- A Floodgate mismatched `key.pem` from the proxy to the backend servers.
- `send-floodgate-data` is enabled without Floodgate being present on all backend servers.
For more information on how to setup Floodgate API on proxy servers, run `!!proxies` in <#613194762249437245>.