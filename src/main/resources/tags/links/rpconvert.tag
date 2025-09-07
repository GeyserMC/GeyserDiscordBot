type: text
title: Converting Java Resource Packs to Bedrock
color: links

---

**Packs for 1.21.3 and below:**
- **For packs with CustomModelData** (like datapack textures, itemsadder or similar), use [Kastle's Converter](https://github.com/Kas-tle/java2bedrock.sh).
- **For general Java resource packs** (only changing vanilla textures), use the [JavaTextureToBedrock](https://rtm516.github.io/ConvertJavaTextureToBedrock/) converter. This tool isn't updated past 1.17, but still may sometimes work on higher versions.
As an alternative, you can manually convert java resource packs. See the pinned messages in https://discord.com/channels/613163671870242838/1139296287179677857 for more info.

**Packs for 1.21.4 and above:**
For the following tools you'll need to use the **Custom Item API V2 Preview builds of Geyser** from [here](https://github.com/GeyserMC/Geyser/pull/5189).
:warning: Caution: Both the Geyser Preview and these converters are experimental!
- **For items with CustomModelData** (like datapack textures, itemsadder or similar), use [Rainbow](https://github.com/GeyserMC/Rainbow). Run !!rainbow for more info.
- **For general Java resource packs** (only changing vanilla textures) use [Thunder](https://github.com/GeyserMC/PackConverter). Run !!thunder for more info.
As an alternative, you will have to manually convert with either Custom Item API V1 or Custom Item API V2. See the pinned messages in https://discord.com/channels/613163671870242838/1139296287179677857 for more info.

**Custom GUIs on Bedrock**
Custom GUIs on Bedrock need to be done using JsonUI, however, you can use this tool to upload your GUI pngs & their unicodes 
and convert them into a Bedrock resource pack to use in Geyser's `packs` folder:
https://abishekbhusal.com/j2b_gui/
If you'd like to manually convert custom GUIs using JsonUI, here are some helpful links:
https://wiki.bedrock.dev/json-ui/json-ui-documentation
https://github.com/ofunny/ofunnysBedrockExamples/tree/main/geysermc.chestBackgroundExample