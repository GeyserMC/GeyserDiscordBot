type: text
aliases: packs, texturepacks, resourcepack, resourcepacks, rp, texturepack
title: Adding Resourcepacks To Geyser
color: info

---

To add a Bedrock resource pack to your server, take a .zip or .mcpack file and put it in Geyser's `packs` folder. Which is in the following location:
- **Fabric/NeoForge**: `/config/Geyser-<platform>/packs/`
- **Geyser Standalone**: `/packs/` at the root directory
- **Other platforms**: `/plugins/Geyser-<platform>/packs/`

If it also contains a custom mappings file, put it in the `custom_mappings` folder. Which is also located in:
- **Fabric/NeoForge**: `/config/Geyser-<platform>/custom_mappings/`
- **Geyser Standalone**: `/custom_mappings/` at the root directory
- **Other platforms**: `/plugins/Geyser-<platform>/custom_mappings/`

The resource pack needs to be a Bedrock resource pack, and if you only have it in Java format, you can do `!!rpconvert` in <#613194762249437245> for info about converting Java resource packs to Bedrock ones.