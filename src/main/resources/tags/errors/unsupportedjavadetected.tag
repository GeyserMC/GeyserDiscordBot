type: issue-only
issues: Unsupported Java detected (60.0)

---

Old server versions do not support Java 16, which Geyser requires. Paper 1.15.2 and higher can be forced to use Java 16 by adding `-DPaper.IgnoreJavaVersion=true`
to the JVM startup flags.
Servers on 1.8.8 can use Java 16 if `use-native-transport` is set to `false` in `server.properties`, although Geyser
does not support versions below 1.12.2
