# Tags
This directory contains the tags that the bot will use. `.tag` files contain the tag data, they are Discord Markdown files with a little header for settings.

## Format
All tags follow this format:
```markdown
type: text

---

Some nice Discord Markdown message
```
See [here](https://support.discord.com/hc/en-us/articles/210298617-Markdown-Text-101-Chat-Formatting-Bold-Italic-Underline-) for information on Discord Markdown

## Type: Text
The below lays out the `text` tag type, allows for Markdown to be sent as an embed. It also has an optional `aliases` attribute which allows you to define aliases for a tag. Additionally, an optional attribute is `image`, which will display an image at the bottom of the embed. Finally, you can use `button` (up to 5) to specify a button, using markdown link syntax.

```markdown
type: text
aliases: test1, test2
image: https://example.com/example.png
button: [View Rory](https://example.com)
button: [Floodagte Wiki](https://wiki.geysermc.org/floodgate/)

---

Test
```

## Type: Raw Text 
The below lays out the `text-raw` tag type, allows for Markdown to be sent as a basic message. It also has an optional `aliases` attribute which allows you to define aliases for a tag. The `button` tag is also supported here.

```markdown
type: text-raw

---

Test
```
