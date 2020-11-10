const stringSimilarity = require('string-similarity')
const Discord = require('discord.js')
const Utils = require('../../utils')

/**
 * Handle the tag command
 *
 * @param {Discord.Message} msg The original message send by the user
 * @param {Array} args The list of arguments passed to the command
 */
exports.handleTagCommand = async (msg, args) => {
  const embed = new Discord.MessageEmbed()

  // Check to make sure we have a tag
  if (args.length <= 1) {
    embed.setTitle('Invalid usage')
    embed.setDescription('Missing tag to check. `!tag <tag>`')
    embed.setColor(0xff0000)
    msg.channel.send(embed)
    return
  }

  // Remove the first argument (the command)
  args.splice(0, 1)

  // Join the other arguments together and collect the tags
  const query = args.join(' ').trim()
  const tags = await getTags()
  let foundTag = false

  // Search the tags by an exact starting match
  tags.forEach((tag) => {
    if (tag.name.toLowerCase().startsWith(query.toLowerCase())) {
      foundTag = true

      sendTagEmbed(msg, tag)
    }
  })

  // If we haven't found a tag yet use an
  // algorythm to check for the most similar tag
  if (!foundTag) {
    let similar = stringSimilarity.findBestMatch(query.toLowerCase(), tags.map(a => a.name.toLowerCase()))
    similar = similar.bestMatch

    // Make sure the rating is over 20%
    if (similar.rating >= 0.2) {
      const tag = tags.find(a => a.name.toLowerCase() === similar.target)
      foundTag = true

      sendTagEmbed(msg, tag)
    }
  }

  // Send a message if we dont know what tag
  if (!foundTag) {
    embed.setTitle('Unknown tag')
    embed.setDescription('That tag is not on our GitHub so it is untested!')
    embed.setColor(0xff0000)
    msg.channel.send(embed)
  }
}

/**
 * Build an embed and send it based on the passed tag details
 *
 * @param {Discord.Message} msg The original message sent by the use
 * @param {Object} tag The tag to use for the embed contents
 */
function sendTagEmbed (msg, tag) {
  const embed = new Discord.MessageEmbed()

  // Build an embed for the tag
  embed.setTitle(tag.name)
  embed.setColor(0x00ff00)
  embed.setURL(tag.url)
  embed.addField('Category', tag.category)
  embed.addField('Instructions', tag.instructions)

  // Discord doesnt support sending multiple embeds
  // per message via the client api
  msg.channel.send(embed)
}

/**
 * Get the tags from the github page
 * and parse + format the results
 *
 * @returns {Array} The list of tag objects with name, url, instructions and category
 */
async function getTags () {
  // Fetch the search page
  const { status, data: contents } = await Utils.getContents('https://raw.githubusercontent.com/wiki/GeyserMC/Geyser/Tags.md')

  // Make sure we got a response
  if (contents === '' || status !== 200) {
    return
  }

  let category = ''
  const tags = []

  contents.split('\n').forEach(line => {
    line = line.trim()

    // Check for a header line
    if (line.startsWith('## ')) {
      category = line.replace('## ', '')
      return
    }

    // Check for a tag line
    if (line.startsWith('* ')) {
      // Get the tag name and url
      const tag = line.match(/\[([A-Za-z \\.-]+)\]\(([a-z\\.\\/:-]+)\)/)

      // Get the inline instructions
      const instructions = line.match(/ \((.+)\)/)

      tags.push({
        name: tag[1],
        url: tag[2],
        instructions: instructions === null ? 'None' : instructions[1],
        category: category
      })
    } else if (line.startsWith('- ')) { // Check for indented instructions
      const tag = tags[tags.length - 1]
      if (tag.instructions === 'None') {
        tag.instructions = line.replace('- ', '').trim()
      } else {
        tag.instructions = (tag.instructions + '\n' + line.replace('- ', '')).trim()
      }
    }
  })

  return tags
}
