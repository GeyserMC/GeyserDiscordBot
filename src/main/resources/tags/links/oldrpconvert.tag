type: text
title: Converting Java Resource Packs To Bedrock (Older Versions)
color: links
button: [Kastle's Converter](https://github.com/Kas-tle/java2bedrock.sh)
button: [JavaTextureToBedrock](https://rtm516.github.io/ConvertJavaTextureToBedrock/)

---

**Pack for 1.21.3 and below:**
- **For packs with CustomModelData** (like datapack textures, itemsadder or similar), use Kastle's Converter.
- **For general Java resource packs** (only changing vanilla textures), use the JavaTextureToBedrock converter. This tool isn't updated past 1.17, but still may sometimes work on higher versions.
As an alternative, you can manually convert java resource packs. See the pinned messages in https://discord.com/channels/613163671870242838/1139296287179677857 for more info.

**Custom GUIs on Bedrock:**
Custom GUIs on Bedrock need to be done using JSON UI, however, you can use this tool to upload your GUI pngs & their unicodes and convert them into a Bedrock resource pack to use in Geyser's `packs` folder: https://abishekbhusal.com/j2b_gui/

If you'd like to manually convert custom GUIs using JSON UI, here are some helpful links:
- https://wiki.bedrock.dev/json-ui/json-ui-documentation
- https://github.com/ofunny/ofunnysBedrockExamples/tree/main/geysermc.chestBackgroundExample
