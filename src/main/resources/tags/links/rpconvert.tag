type: text
title: Converting Java Resource Packs to Bedrock
color: links

---

**Packs for 1.21.3 and below:**
You can use the [JavaTextureToBedrock](https://rtm516.github.io/ConvertJavaTextureToBedrock/) converter to convert general Java resource packs to Bedrock. This tool isn't updated past 1.17, but still may sometimes work.
However you'll need, [Kastle's Converter](https://github.com/Kas-tle/java2bedrock.sh) for packs with CustomModelData.
These will convert to Custom Item API V1, and will therefore not work with Custom Item API V2 (which is in preview builds).

**Packs for 1.21.4 and above:**
You can use [Thunder](https://github.com/GeyserMC/PackConverter) which is also experimental, and is a library for converting general Java Edition resource packs to Bedrock Edition using Custom Item API V2 (which is in preview builds). Run !!thunder for more info.
For items with CustomModelData, you'll need to use [Rainbow](https://github.com/GeyserMC/Rainbow) which is experimental and generates Geyser item mappings and Bedrock resourcepacks for 1.21.4+ packs to Custom Item API V2. Run !!rainbow for more info.
As an alternative, you will have to manually convert with either Custom Item API V1 or Custom Item API V2. Custom Item API V1 may be replaced in the future with Custom Item API V2.

**Custom GUIs on Bedrock**
Custom GUIs on Bedrock need to be done using JsonUI, however, you can use this tool to upload your GUI pngs & their unicodes 
and convert them into a Bedrock resource pack to use in Geyser's `packs` folder:
https://abishekbhusal.com/j2b_gui/
If you'd like to manually convert custom GUIs using JsonUI, here are some helpful links:
https://wiki.bedrock.dev/json-ui/json-ui-documentation
https://github.com/ofunny/ofunnysBedrockExamples/tree/main/geysermc.chestBackgroundExample
