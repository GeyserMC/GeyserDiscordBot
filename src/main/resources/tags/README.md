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
title: Test
color: neutral | success | failure | warning
image: https://example.com/example.png
button: [View Rory](https://example.com)
button: [Floodgate Wiki](https://wiki.geysermc.org/floodgate/)

---

Test
```

## Type: Raw 
The below lays out the `raw` tag type, allows for Markdown to be sent as a basic message. It also has an optional `aliases` attribute which allows you to define aliases for a tag. The `button` tag is also supported here.

```markdown
type: raw

---

Test
```

## Type: Issue only
The below lays out the `issue-only` tag type. This tag will not be registered as a tag that users can run, and only exists to
register responses for issues. It follows the rules that `Issue responses` below sets out.

```markdown
type: issue-only
issues: <issue1> || <issue2> || <issue 3> ...

---

Text
```

## Issue responses
Issue responses work with `text`, `text-raw` and `issue-only` types. Any issues listed are registered to have the content of the tag as the issue response.
Different issues are delimited by `||`, allowing differently worded issues to have the same response.
When a message or log file is shared, it will be scanned to check if it contains any known issues. Responses to the issues found will be sent in a single embed.
`text` and `text-raw` types with issues listed can still be called with the tag command; they do not lose their functionality.
```markdown
type: text
issues: <issue1> || <issue2> || <issue 3> ...

---

Text
```
