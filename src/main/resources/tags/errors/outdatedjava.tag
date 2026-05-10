type: text
aliases: latestjava, unsupportedclassversion, updatejava, openjdk, javadownload
issues: java.lang.UnsupportedClassVersionError
title: :x: Geyser Java Requirements
color: errors

---

This error means your server is not running Java 21 or later which Geyser requires to run.
- You can download Java 21 [here](https://adoptium.net/temurin/releases/).
- To find out how to change your Java version on hosts or other platforms, see [this](https://docs.papermc.io/java-install-update).
- If you’re running a version of Paper that does not support Java 21 or later, you can add the flag `-DPaper.IgnoreJavaVersion=true` to your startup Java arguments to allow Paper to run on Java 21.
- You can run Geyser standalone on another device if your server software cannot be updated to use Java 21.