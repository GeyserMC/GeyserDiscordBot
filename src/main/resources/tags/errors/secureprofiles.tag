type: text
issues: This server requires secure profiles || multiplayer.disconnect.missing_public_key

---

Minecraft 1.19 adds key signing, which is currently only used to verify that chat messages are coming from the right player. 
Floodgate and Geyser currently do not support this, so it should be disabled using the following instructions:

**Spigot, Paper, & all forks**
Set `enforce-secure-profile: false` in [server.properties](https://minecraft.fandom.com/wiki/Server.properties)

**Bungeecord**
Set `enforce_secure_profile: false` in [config.yml](https://www.spigotmc.org/wiki/bungeecord-configuration-guide/)

**Velocity**
Set `force-key-authentication = false` in [velocity.toml](https://github.com/PaperMC/Velocity/blob/dev/3.0.0/proxy/src/main/resources/default-velocity.toml#L19)
