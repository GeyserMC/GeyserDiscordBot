type: text
aliases: loop

---

**This only affects people trying to join Geyser from Windows 10 Edition with Geyser hosted on the same computer.**

This is an issue caused by Loopback restrictions not being lifted. By default, Microsoft Apps have this restriction on all their apps for local connections. You can lift it by typing the following in Windows PowerShell in administrator mode: (it should return OK. if it worked)
`CheckNetIsolation LoopbackExempt -a -n="Microsoft.MinecraftUWP_8wekyb3d8bbwe"`

If this does not work you can try these steps instead:
1. Hold down Windows Key + R
2. In the prompt, type `hdwwiz.exe`, then press Enter then Next
3. Install the Hardware Manually
4. Choose Network Adapter > Next > Microsoft > "Microsoft KM-TEST Loopback Adapter" then hit Next until it's done.
