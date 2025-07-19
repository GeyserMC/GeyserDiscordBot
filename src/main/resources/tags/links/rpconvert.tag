type: text
title: Converting Java Resource Packs to Bedrock
color: neutral

---

**Packs for 1.21.3 and below:**
You can use [Kastle's Converter](https://github.com/Kas-tle/java2bedrock.sh) to convert Java resource packs to Bedrock.
However in some cases, the [JavaTextureToBedrock](https://rtm516.github.io/ConvertJavaTextureToBedrock/) converter may work better for general packs without CustomModelData.
These will convert to Custom Item API V1, and will therefore not work with Custom Item API V2 (which is in preview builds).

**Packs for 1.21.4 and above:**
You will have to manually convert with either Custom Item API V1 or Custom Item API V2 (which is in preview builds). Custom Item API V1 may be replaced in the future with Custom Item API V2.
As an alternative, you can use [Rainbow](https://github.com/GeyserMC/Rainbow) which is experimental and generate Geyser item mappings and Bedrock resourcepacks for 1.21.4+ packs to Custom Item API V2.