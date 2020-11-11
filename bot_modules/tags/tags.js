const Discord = require('discord.js')
const loader = require('./loader')

let tags = {}
let tagList

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

      if (reason !== '') {
        console.error(`The alias tag '${tagName}' has an invalid target (${reason})`)
      }
    }
  })

  console.log(`Loaded ${tagList.length} tags`)
}

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
  embed.setDescription(`\`${tagNameList.join(', ')}\``)
  embed.setFooter('Use "!tag name" to show a tag')

  msg.channel.send(embed)
}

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
  const tagName = args[1]

  // Check if the tag exists
  if (!tagList.includes(tagName)) {
    const embed = new Discord.MessageEmbed()
    embed.setTitle('Missing tag')
    embed.setDescription(`No tag with the name \`${tagName}\`, do \`!tags\` for the full list.\nAdd a search of possible ones here? (Using the same simmilarity check as the provider command)`)
    embed.setColor(0xff0000)
    msg.channel.send(embed)
    return
  }

  // Get the tag and contents
  const tag = tags[tagName]
  let content = tag.content

  // Get the target contents if its an alias
  if (tag.type === 'alias') {
    content = tags[tag.target].content
  }

  msg.channel.send(content)
}
