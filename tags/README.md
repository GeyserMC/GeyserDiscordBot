# Tags
This directory contains the tags that the bot will use. `.tag` files contain the tag data, they are Discord Markdown files with a little header for settings.

## Format
All tags follow this format:
```markdown
type text

---

Some nice Discord Markdown message
```
See [here](https://support.discord.com/hc/en-us/articles/210298617-Markdown-Text-101-Chat-Formatting-Bold-Italic-Underline-) for infomation on Discord Markdown

## Type: Alias
The below lays out the `alias` tag type, allows for 1 tag to be masked as another.
```markdown
type: alias
target: rules
```

## Type: Text
The below lays out the `text` tag type, allows for Markdown to be sent as an embed. It also has an optional `image` attribute.
```markdown
type: text
image: https://example.com/example.png

---

Test
```

## Type: Raw Text 
The below lays out the `text-raw` tag type, allows for Markdown to be sent as a basic message.
```markdown
type: text-raw

---

Test
```