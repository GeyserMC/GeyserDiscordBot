type: text
aliases: pnl

---

**Non-warden setups (Pelican / Pterodactyl / etc.):**

1. Allocate/add an extra port in your panel.
2. In `config.yml`, set `bedrock.port` to that new port.
3. Ensure `signaling.mode` is set to `builtin` or `hybrid`.
4. Save and restart the server.
5. Type `!!signal` here and follow the steps.
6. Connect using your Java IP and the new port.
