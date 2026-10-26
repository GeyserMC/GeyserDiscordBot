type: text
issues: InitialConnection-13
title: "NetherNet" error code
color: failure

---
InitialConnection: `13`
ClientReason: `LegacyDisconnect`
ProtocolConstant: `LEGACY_DISCONNECT`

The client attempts to connect via NetherNet first, then falls back to RakNet if the initial connection probe fails. This error is displayed if both attempts time out.