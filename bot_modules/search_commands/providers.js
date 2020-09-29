const stringSimilarity = require('string-similarity')
const Discord = require('discord.js')
const Utils = require('../../utils')

/**
 * Handle the provider command
 *
 * @param {Discord.Message} msg The original message send by the user
 * @param {Array} args The list of arguments passed to the command
 */
exports.handleProviderCommand = async (msg, args) => {
  const embed = new Discord.MessageEmbed()

  // Check to make sure we have a provider
  if (args.length <= 1) {
    embed.setTitle('Invalid usage')
    embed.setDescription('Missing provider to check. `!provider <provider>`')
    embed.setColor(0xff0000)
    msg.channel.send(embed)
    return
  }

  // Remove the first argument (the command)
  args.splice(0, 1)

  // Join the other arguments together and collect the providers
  const query = args.join(' ').trim()
  const providers = await getProviders()
  let foundProvider = false

  // Search the providers by an exact starting match
  providers.forEach((provider) => {
    if (provider.name.toLowerCase().startsWith(query.toLowerCase())) {
      foundProvider = true

      sendProviderEmbed(msg, provider)
    }
  })

  // If we haven't found a provider yet use an
  // algorythm to check for the most similar provider
  if (!foundProvider) {
    let similar = stringSimilarity.findBestMatch(query.toLowerCase(), providers.map(a => a.name.toLowerCase()))
    similar = similar.bestMatch

    // Make sure the rating is over 20%
    if (similar.rating >= 0.2) {
      const provider = providers.find(a => a.name.toLowerCase() === similar.target)
      foundProvider = true

      sendProviderEmbed(msg, provider)
    }
  }

  // Send a message if we dont know what provider
  if (!foundProvider) {
    embed.setTitle('Unknown provider')
    embed.setDescription('That provider is not on our GitHub so it is untested!')
    embed.setColor(0xff0000)
    msg.channel.send(embed)
  }
}

/**
 * Build an embed and send it based on the passed provider details
 *
 * @param {Discord.Message} msg The original message sent by the use
 * @param {Object} provider The provider to use for the embed contents
 */
function sendProviderEmbed (msg, provider) {
  const embed = new Discord.MessageEmbed()

  // Build an embed for the provider
  embed.setTitle(provider.name)
  embed.setColor(0x00ff00)
  embed.setURL(provider.url)
  embed.addField('Category', provider.category)
  embed.addField('Instructions', provider.instructions)

  // Discord doesnt support sending multiple embeds
  // per message via the client api
  msg.channel.send(embed)
}

/**
 * Get the providers from the github page
 * and parse + format the results
 *
 * @returns {Array} The list of provider objects with name, url, instructions and category
 */
async function getProviders () {
  // Fetch the search page
  const { status, data: contents } = await Utils.getContents('https://raw.githubusercontent.com/wiki/GeyserMC/Geyser/Supported-Hosting-Providers.md')

  // Make sure we got a response
  if (contents === '' || status !== 200) {
    return
  }

  let category = ''
  const providers = []

  contents.split('\n').forEach(line => {
    line = line.trim()

    // Check for a header line
    if (line.startsWith('## ')) {
      category = line.replace('## ', '')
      return
    }

    // Check for a provider line
    if (line.startsWith('* ')) {
      // Get the provider name and url
      const provider = line.match(/\[([A-Za-z \\.-]+)\]\(([a-z\\.\\/:-]+)\)/)

      // Get the inline instructions
      const instructions = line.match(/ \((.+)\)/)

      providers.push({
        name: provider[1],
        url: provider[2],
        instructions: instructions === null ? 'None' : instructions[1],
        category: category
      })
    } else if (line.startsWith('- ')) { // Check for indented instructions
      const provider = providers[providers.length - 1]
      provider.instructions = (provider.instructions + '\n' + line.replace('- ', '')).trim()
    }
  })

  return providers
}
