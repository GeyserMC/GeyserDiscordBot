type: text
issues: java.lang.NullPointerException: Cannot invoke "org.geysermc.platform.spigot.GeyserSpigotConfiguration.getRemote()" because "this.geyserConfig" is null

---

This means that you configured the java remote ip in the config.yml with a value that is not a valid ip address. In most cases leaving it on "auto" will work fine as this option will automatically bind to the server ip address.