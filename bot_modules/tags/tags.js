const Discord = require('discord.js')
const loader = require('./loader')

let tags = {}
let aliases = {}
let tagList = []

/**
 * Initialise the tag system
 */
exports.initTags = () => {
  // Load tags from file
  const data = loader.load()
  tags = data.tags
  aliases = data.aliases

  // Cache the list of tag names
  tagList = Object.keys(tags)
  tagList.sort()

  // Check we dont have any aliases and tags with the same name
  let lastTagChecked
  if (Object.keys(tags).some(item => { lastTagChecked = item; return Object.keys(aliases).includes(item) })) {
    throw new Error('1 or more tags also registered as an alias! ' + lastTagChecked)
  }

  console.log(`Loaded ${tagList.length} tags`)
}

/**
 * Handle the tags command
 *
 * @param {Discord.Message} msg The original message send by the user
 * @param {Array} args The list of arguments passed to the command
 */
exports.handleTagsCommand = async (msg, args) => {
  // Print all tags
  // Ignore args, unless we want search?
  const embed = new Discord.MessageEmbed()

  let search = ''

  if (args.length >= 2) {
    search = args[1]
  }

  const tagNameList = []
  tagList.forEach(tagName => {
    if (tagName.includes(search)) {
      tagNameList.push(tagName)
    }
  })

  if (tagNameList.length === 0) {
    embed.setColor(0xff0000)
    embed.setTitle('No tags found.')
    embed.setDescription('No tags were found for your search.')
    embed.setFooter('Use "!tag aliases name" to see all the aliases for a certain tag')
  } else {
    embed.setColor(0x00ff00)
    embed.setTitle(`Tags (${tagNameList.length})`)
    embed.setDescription(`\`${tagNameList.join('`, `')}\``)
    embed.setFooter('Use "!tag name" to show a tag')
  }

  msg.channel.send(embed)
}

/**
 * Handle the tag command
 *
 * @param {Discord.Message} msg The original message send by the user
 * @param {Array} args The list of arguments passed to the command
 */
exports.handleTagCommand = async (msg, args) => {
  const embed = new Discord.MessageEmbed()

  // Check we were sent a tag
  if (args.length <= 1) {
    embed.setTitle('Invalid usage')
    embed.setDescription('Missing tag name. `!tag <name>`')
    embed.setColor(0xff0000)
    msg.channel.send(embed)
    return
  }

  // Get the tag name
  let tagName = args[1].toLowerCase()

  // Check for invalid characters
  if (!tagName.match(/^[\w-]+$/)) {
    embed.setTitle('Invalid tag')
    embed.setDescription('Invalid characters in the requested tag')
    embed.setColor(0xff0000)
    msg.channel.send(embed)
    return
  }

  let showAliases = false
  if (tagName === 'aliases' || tagName === 'alias') {
    if (args.length <= 2) {
      embed.setTitle('Invalid usage')
      embed.setDescription(`Missing tag name. \`!tag ${tagName} <name>\``)
      embed.setColor(0xff0000)
      msg.channel.send(embed)
      return
    }

    tagName = args[2].toLowerCase()
    showAliases = true
  }

  if (tagName in aliases) {
    tagName = aliases[tagName]
  }

  // Check if the tag exists
  if (!tagList.includes(tagName)) {
    embed.setTitle('Missing tag')
    embed.setDescription(`No tag with the name \`${tagName}\`, do \`!tags\` for the full list.`)
    embed.setColor(0xff0000)
    msg.channel.send(embed)
    return
  }

  // Get the tag and contents
  const tag = tags[tagName]

  if (showAliases) {
    // Check if the alias list is empty
    if (tag.aliases.length === 0) {
      embed.setTitle(`No aliases for ${tagName}`)
      embed.setDescription(`No aliases where found for the tag with the name \`${tagName}\`.`)
      embed.setColor(0xff0000)
      msg.channel.send(embed)
      return
    }

    // Build the embed for the list of aliases
    embed.setColor(0x00ff00)
    embed.setTitle(`Aliases for ${tagName} (${tag.aliases.length})`)
    embed.setDescription(`\`${tag.aliases.join('`, `')}\``)
    embed.setFooter('Use "!tag name" to show a tag')
  } else {
    embed.setColor(0x00ff00)
    embed.setDescription(tag.content)

    // Set the image if we have one
    if (tag.image !== '') {
      embed.setImage(tag.image)
    }
  }

  // Send the tag content
  if (tag.type === 'text-raw') {
    msg.channel.send(tag.content)
  } else {
    msg.channel.send(embed)
  }
}
