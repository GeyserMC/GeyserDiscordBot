type: text
issues: InitialConnection-32 || Kelp for more info
aliases: kelpcode
title: "Kelp" error code
color: failure

---
InitialConnection: `32`
ClientReason: `Timeout`
ProtocolConstant: `TIMEOUT`

This error appears when the Bedrock client timed out when trying to connect to your server, which could occur due to a variety of reasons.
If you have not already, follow our [setup instructions](https://wiki.geysermc.org/geyser/setup/). Then, check the server console for any errors.
To verify Bedrock clients can connect, you can try running the `geyser connectiontest <ip> <port>` command with your server IP and Geyser port.

Additionally, there are various fixes for this on our wiki, please see [our guide](https://wiki.geysermc.org/geyser/fixing-unable-to-connect-to-world/).