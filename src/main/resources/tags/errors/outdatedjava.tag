type: text
aliases: j17, java17, latestjava, unsupportedclassversion, updatejava, openjdk, javadownload
issues: java.lang.UnsupportedClassVersionError
title: :x: Geyser Java Requirements
color: errors

---

This error means your server is not running Java 17 or later which Geyser requires to run.
- You can download Java 17 at https://adoptium.net/temurin/releases/
- To find out how to change your Java version on hosts or other platforms, look at https://docs.papermc.io/java-install-update
- If you’re running a version of Paper that does not support Java 17 or later, you can add the flag `-DPaper.IgnoreJavaVersion=true` to your startup Java arguments to allow Paper to run on Java 17.
- You can run Geyser standalone on another device if a server software cannot be updated to use Java 17.