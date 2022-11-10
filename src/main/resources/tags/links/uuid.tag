type: text
aliases: xuid, floodgateuuid, bedrockuuid

---
Automatic method:
Use https://uuid.kejona.dev/ to convert any Bedrock Player username into a Floodgate UUID.

Manual method:
Unfortunately, the floodgateuuid converter currently does not work. In the meantime, you can manually find out a uuid using this method:
Step 1: Visit https://www.cxkes.me/xbox/xuid and enter the bedrock username (without the period that is added by Geyser)
Step 2: Search for the hexidecimal result and copy it down
Step 3: You can try just that number instead of the username, but mine was formatted like this

XUID Search Result: 000901F4BF5B7415
Minecraft XUID: 00000000-0000-0000-0009-01f4bf5b7415

So step 4 would be to add a dash after the first four digits and then paste "00000000-0000-0000-" before the search result to get the correct format.
