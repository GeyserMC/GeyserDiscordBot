type: text
hidden: true
issues: InitialConnection-46
title: "Guardian" error code
color: failure

---
InitialConnection: `46`
ClientReason: `NotAuthenticated`
ProtocolConstant: `NOT_AUTHENTICATED`

This error appears when the Bedrock client was unable to login to your Microsoft account. Please see [Mojang's guide](https://help.minecraft.net/hc/en-us/articles/360045828292) on how to resolve this issue.
Additional information: When using NetherNet, this error indicates that the server rejected the connection due to an invalid identity (e.g., a mismatch between the transport and login packets) or a self-signed identity. It is unclear whether this error is always observable, as the client may reject the connection before the server receives it.