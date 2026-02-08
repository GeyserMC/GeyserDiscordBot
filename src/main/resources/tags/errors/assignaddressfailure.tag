type: issue-only
issues: java.net.BindException: Cannot assign requested address

---

Your Geyser server is trying to use an IP address that isn't available or is being blocked by the operating system or firewall.
- In your Geyser config, check the bedrock section and make sure the `address` is set to `0.0.0.0`.