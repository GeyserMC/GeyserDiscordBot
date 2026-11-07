type: text
hidden: true
issues: InitialConnection-149
title: "Terracotta" error code
color: failure

---
InitialConnection: `149`
ClientReason: `UnsupportedTransport`
ProtocolConstant: `UNSUPPORTED_TRANSPORT`

This issue occurs when the transport protocol is unsupported. In version 26.60 and later, this message may also appear if the signaling server is unreachable or not running.

This is most likely a setup issue, please see <#1564652375300640910>.