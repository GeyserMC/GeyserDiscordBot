const Discord = require('discord.js')
const loader = require('./loader')

let tags = {}
let tagList

/**
 * Initialise the tag system
 */
exports.initTags = () => {
  // Load tags from file
  tags = loader.load()

  // Cache the list of tag names
  tagList = Object.keys(tags)

  // Check all aliases are valid
  tagList.forEach(tagName => {
    const tag = tags[tagName]
    if (tag.type === 'alias') {
      let reason = ''
      if (!tagList.includes(tag.target)) {
        reason = 'missing, ' + tag.target
      } else if (tags[tag.target].type === 'alias') {
        reason = 'alias'
      }

      // Log the error if we have one
      if (reason !== '') {
        throw new Error(`The alias tag '${tagName}' has an invalid target (${reason})`)
      }
    }
  })

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

  const tagNameList = []
  tagList.forEach(tagName => {
    if (tags[tagName].type !== 'alias') {
      tagNameList.push(tagName)
    }
  })

  embed.setColor(0x00ff00)
  embed.setTitle(`Tags (${tagNameList.length})`)
  embed.setDescription(`\`${tagNameList.join('`, `')}\``)
  embed.setFooter('Use "!tag name" to show a tag')

  msg.channel.send(embed)
}

/**
 * Handle the tag command
 *
 * @param {Discord.Message} msg The original message send by the user
 * @param {Array} args The list of arguments passed to the command
 */
exports.handleTagCommand = async (msg, args) => {
  // Check we were sent a tag
  if (args.length <= 1) {
    const embed = new Discord.MessageEmbed()
    embed.setTitle('Invalid usage')
    embed.setDescription('Missing tag name. `!tag <name>`')
    embed.setColor(0xff0000)
    msg.channel.send(embed)
    return
  }

  // Get the tag name
  let tagName = args[1].toLowerCase()

  let showAliases = false
  if (tagName === 'aliases' || tagName === 'alias') {
    if (args.length <= 2) {
      const embed = new Discord.MessageEmbed()
      embed.setTitle('Invalid usage')
      embed.setDescription(`Missing tag name. \`!tag ${tagName} <name>\``)
      embed.setColor(0xff0000)
      msg.channel.send(embed)
      return
    }

    tagName = args[2].toLowerCase()
    showAliases = true
  }

  // Check if the tag exists
  if (!tagList.includes(tagName)) {
    const embed = new Discord.MessageEmbed()
    embed.setTitle('Missing tag')
    embed.setDescription(`No tag with the name \`${tagName}\`, do \`!tags\` for the full list.`)
    embed.setColor(0xff0000)
    msg.channel.send(embed)
    return
  }

  // Get the tag and contents
  const tag = tags[tagName]
  let content = tag.content

  if (showAliases) {
    // Check we are not checking an alias tag
    if (tag.type === 'alias') {
      const embed = new Discord.MessageEmbed()
      embed.setTitle('Invalid usage')
      embed.setDescription('You cannot check the aliases of an alias.')
      embed.setColor(0xff0000)
      msg.channel.send(embed)
      return
    }

    const aliases = []
    tagList.forEach(tmpTagName => {
      const tmpTag = tags[tmpTagName]
      if (tmpTag.type === 'alias' && tmpTag.target === tagName) {
        aliases.push(tmpTagName)
      }
    })

    // Check if the alias list is empty
    if (aliases.length === 0) {
      const embed = new Discord.MessageEmbed()
      embed.setTitle(`No aliases for ${tagName}`)
      embed.setDescription(`No aliases where found for the tag with the name \`${tagName}\`.`)
      embed.setColor(0xff0000)
      msg.channel.send(embed)
      return
    }

    // Build the embed for the list of aliases
    const embed = new Discord.MessageEmbed()
    embed.setColor(0x00ff00)
    embed.setTitle(`Aliases for ${tagName} (${aliases.length})`)
    embed.setDescription(`\`${aliases.join(', ')}\``)
    embed.setFooter('Use "!tag name" to show a tag')

    content = embed
  } else {
    // Get the target contents if its an alias
    if (tag.type === 'alias') {
      content = tags[tag.target].content
    }
  }

  // Send the tag content
  msg.channel.send(content)
}
