type: text
hidden: true
issues: InitialConnection-90
title: "Block" error code
color: failure

---
InitialConnection: `90`
ClientReason: `BadPacket`
ProtocolConstant: `BAD_PACKET`

This error appears when the Bedrock client receives an invalid packet. Please ensure you are running the latest version of Geyser. Additionally, provide a log dump using !!logsdump. If you are unable to generate a dump, please send your logs using !!logs instead.
Additional information: This issue occurs when the server sends a malformed or invalid packet.