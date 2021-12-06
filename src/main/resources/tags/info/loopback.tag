type: text
aliases: loopback, windows10

---
Windows 10

Using Geyser on the same computer

This only affects people trying to join Geyser from Windows 10 Edition with Geyser hosted on the same computer.

This is an issue caused by Loopback restrictions not being lifted. By default, Microsoft Apps have this restriction on all their apps for local connections. Geyser will attempt to resolve this automatically; however, if you're still having connection problems, you can lift it by typing the following in Windows PowerShell in administrator mode: (it should return OK. if it worked)

CheckNetIsolation LoopbackExempt -a -n="Microsoft.MinecraftUWP_8wekyb3d8bbwe"
Should this not work, you can try this set of steps:

Hold down Windows Key + R
In the prompt, type hdwwiz.exe, then press Enter then Next
Install the Hardware Manually
Choose Network Adapter > Next > Microsoft > "Microsoft KM-TEST Loopback Adapter" then hit Next until it's done.