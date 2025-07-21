type: text
title: Custom Items on 1.21.4 and above
color: neutral

---

Geyser's custom item system has not yet been updated to fully support Java's 1.21.4 item model definition format. There is a PR open for a [new custom item API](https://github.com/GeyserMC/Geyser/pull/5189), which adds support for 1.21.4 item definitions, their predicates, items with custom components, and more.
This is currently available as a Preview at <#1230530815918866453>, more details can be found there.

Until this PR is merged, you can still use the current custom item API with the 1.21.4 format, by using `range_dispatch` conditions (checking for index 0 of the `custom_model_data` property) in Java resource packs. Note that you will not be able to use these resource packs with converters like Kastle's java2bedrock.sh.
