type: text
title: :x: IP Forwarding Error
aliases: ipforward
issues: please enable it in your BungeeCord config as well
color: errors

---

This error is caused either by:
- Not having `ip_forward` enabled in your BungeeCord `config.yml`.
- [Player information forwarding](https://docs.papermc.io/velocity/player-information-forwarding/) isn't set up on Velocity.
- A Floodgate mismatched `key.pem` from the proxy to the backend servers.
- `send-floodgate-data` being enabled without Floodgate being present on all backend servers.
For more information on how to setup Floodgate API on proxies, run `!!proxies` in <#613194762249437245>.