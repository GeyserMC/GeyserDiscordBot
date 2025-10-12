type: text
aliases: loopback, windows
title: :information_source: Joining A Geyser Server When Hosted On The Same Windows Machine
color: info

---

This only affects people trying to join Geyser servers using Minecraft: Bedrock Edition for Windows with Geyser hosted on the same Windows machine.

This issue occurs when Loopback restrictions haven’t been lifted. By default, Microsoft Apps block local connections, and while Geyser attempts to fix this automatically, you may still need to do it manually. To lift the restriction, open Windows PowerShell as Administrator and run:
`CheckNetIsolation LoopbackExempt -a -n="Microsoft.MinecraftUWP_8wekyb3d8bbwe"`

If the above solution does not work, you can try this set of steps:
1. Hold down Windows Key + R
2. In the prompt, type hdwwiz.exe, then press Enter then Next
3. Install the Hardware Manually
4. Choose Network Adapter > Next > Microsoft > "Microsoft KM-TEST Loopback Adapter" then hit "Next" until it's done.