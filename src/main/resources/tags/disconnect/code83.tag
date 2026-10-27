type: text
hidden: true
issues: InitialConnection-83
aliases: advertiseaddresses
title: "Door" error code
color: failure

---
InitialConnection: `83`
ClientReason: `NetherNetICE`
ProtocolConstant: `CONN_ICE`

This error usually occurs when your Geyser server is running behind a NAT setup. Please try adding your public IP address to the `advertise-addresses` config option, then restart your server.