type: text
aliases: loopback, windows

---
This only affects people trying to join Geyser from Windows Edition with Geyser hosted on the same Windows machine.

This is an issue caused by Loopback restrictions not being lifted. By default, Microsoft Apps have this restriction on all their apps for local connections. Geyser will attempt to resolve this automatically; however, if you're still having connection problems, you can lift it by typing the following in Windows PowerShell in administrator mode:
`CheckNetIsolation LoopbackExempt -a -n="Microsoft.MinecraftUWP_8wekyb3d8bbwe"`
If the command was executed successfully, it wil return "OK". Then, restart your Bedrock client and try connecting again.

Should this not work, you can try this set of steps:
1. Hold down Windows Key + R
2. In the prompt, type hdwwiz.exe, then press Enter then Next
3. Install the Hardware Manually
4. Choose Network Adapter > Next > Microsoft > "Microsoft KM-TEST Loopback Adapter" then hit "Next" until it's done.