type: text
issues: java.net.BindException: Address already in use

---

This means something (likely another instance of Geyser) is running on the port you have specified in the config. Please make sure you close all applications running on this port. If you don't recall opening anything, usually restarting your computer fixes this.